/*
 *   This program is is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at
 *   your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */

/**
 * $Id$
 * 
 * @file rlm_jradius.c
 * @brief rlm_jradius - The FreeRADIUS JRadius Server Module
 * 
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 * Copyright (c) 2007-2010 Coova Technologies, LLC 
 * 
 * This module is used to connect FreeRADIUS to the JRadius server. 
 * JRadius is a Java RADIUS client and server framework, see doc/rlm_jradius
 * and http://www.coova.org/JRadius for more information. 
 *
 * Author(s): David Bird <david@coova.com>
 *
 * Connection pooling code based on rlm_sql, see rlm_sql/sql.c for copyright and license.
 */

#include <freeradius-devel/radiusd.h>
#include <freeradius-devel/modules.h>
#include <freeradius-devel/rad_assert.h>
#include <freeradius-devel/libradius.h>
#include <freeradius-devel/conffile.h>
#include <freeradius-devel/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/signal.h>
#include <sys/types.h>
#include <fcntl.h>
#include <poll.h>

#ifdef HAVE_PTHREAD_H
#include <pthread.h>
#endif

#ifdef HAVE_SYS_SOCKET_H
#include <sys/socket.h>
#endif

#ifndef O_NONBLOCK
#define O_NONBLOCK O_NDELAY
#endif

static const int JRADIUS_PORT         = 1814;
static const int HALF_MESSAGE_LEN     = 16384;
static const int MESSAGE_LEN          = 32768;

static const int JRADIUS_authenticate = 1;
static const int JRADIUS_authorize    = 2;
static const int JRADIUS_preacct      = 3;
static const int JRADIUS_accounting   = 4;
static const int JRADIUS_checksimul   = 5;
static const int JRADIUS_pre_proxy    = 6;
static const int JRADIUS_post_proxy   = 7;
static const int JRADIUS_post_auth    = 8;
#ifdef WITH_COA
static const int JRADIUS_recv_coa     = 9;
static const int JRADIUS_send_coa     = 10;
#endif

#define LOG_PREFIX  "rlm_jradius: "
#define MAX_HOSTS   4

#define VENDOR_ID(x)            (((x) >> 16) & 0xffff)
#define ATTRIBUTE_ID(x)         ((x) & 0xffff)
#define ATTRIBUTE_TYPE(h,l)     ( ((h) << 16) | ((l) & 0xffff) )

typedef struct byte_array
{
  unsigned int  size;
  unsigned int  pos;
  unsigned int  left;
  unsigned char *b;
} byte_array_t;

typedef struct jradius_socket {
  int  id;
#ifdef HAVE_PTHREAD_H
  pthread_mutex_t mutex;
#endif
  struct jradius_socket *next;
  enum { is_connected, not_connected } state;
  
  union {
    int sock;
  } con;

  byte_array_t* ba;
} jradius_socket_t;

typedef struct rlm_jradius {
  time_t            connect_after;
  jradius_socket_t  *socket_pool;
  jradius_socket_t  *last_socket_used;

  char const        *name;
  char const        *host[MAX_HOSTS];

  uint32_t          ipaddr[MAX_HOSTS];
  int               port[MAX_HOSTS];

  uint32_t          timeout;
  uint32_t          read_timeout;
  uint32_t          write_timeout;

  bool              allow_codechange;
  bool              allow_idchange;

  int               onfail;
  char const        *onfail_s;

  bool              keepalive;
  uint32_t          socket_cnt;
} rlm_jradius_t;

static const CONF_PARSER module_config[] = {
  { "name",             FR_CONF_OFFSET(PW_TYPE_STRING, rlm_jradius_t, name), "localhost" },
  { "primary",          FR_CONF_OFFSET(PW_TYPE_STRING, rlm_jradius_t, host[0]), "localhost" },
  { "secondary",        FR_CONF_OFFSET(PW_TYPE_STRING, rlm_jradius_t, host[1]), NULL },
  { "tertiary",         FR_CONF_OFFSET(PW_TYPE_STRING, rlm_jradius_t, host[2]), NULL },
  { "timeout",          FR_CONF_OFFSET(PW_TYPE_INTEGER, rlm_jradius_t, timeout), "5" },
  { "read_timeout",     FR_CONF_OFFSET(PW_TYPE_INTEGER, rlm_jradius_t, read_timeout), "90" },
  { "write_timeout",    FR_CONF_OFFSET(PW_TYPE_INTEGER, rlm_jradius_t, write_timeout), "90" },
  { "onfail",           FR_CONF_OFFSET(PW_TYPE_STRING, rlm_jradius_t, onfail_s), NULL },
  { "keepalive",        FR_CONF_OFFSET(PW_TYPE_BOOLEAN, rlm_jradius_t, keepalive), "yes" },
  { "connections",      FR_CONF_OFFSET(PW_TYPE_INTEGER, rlm_jradius_t, socket_cnt), "8" },
  { "allow_codechange", FR_CONF_OFFSET(PW_TYPE_BOOLEAN, rlm_jradius_t, allow_codechange), "no" },
  { "allow_idchange",   FR_CONF_OFFSET(PW_TYPE_BOOLEAN, rlm_jradius_t, allow_idchange), "no" },
  CONF_PARSER_TERMINATOR
};

static int sock_read(rlm_jradius_t *inst, jradius_socket_t *jrsock, uint8_t *b, size_t blen) {
  if (jrsock->ba) {
    size_t idx = 0;

    while (idx < blen) {
      b[idx] = jrsock->ba->b[jrsock->ba->pos];
      jrsock->ba->pos++;
      idx++;
    }

    return blen;
  } else {
    int fd = jrsock->con.sock;
    int timeout = inst->read_timeout;
    struct timeval tv;
    ssize_t c;
    size_t recd = 0;
    fd_set fds;

    while (recd < blen) {

      tv.tv_sec = timeout;
      tv.tv_usec = 0;
      
      FD_ZERO(&fds);
      FD_SET(fd, &fds);
      
      if (select(fd + 1, &fds, (fd_set *) 0, (fd_set *) 0, &tv) == -1)
        return -1;
      
      if (FD_ISSET(fd, &fds))
  #ifdef WIN32
        c = recv(fd, b + recd, blen-recd, 0);
  #else
        c = read(fd, b + recd, blen-recd);
  #endif
      else
        return -1;

      if (c <= 0) return -1;
      recd += c;
    }

    if (recd < blen) return -1;
    return recd;
  }
}

static int sock_write(rlm_jradius_t *inst, jradius_socket_t *jrsock, char *b, size_t blen) {
  int fd = jrsock->con.sock;
  int timeout = inst->write_timeout;
  struct timeval tv;
  ssize_t c;
  size_t sent = 0;
  fd_set fds;

  while (sent < blen) {

    tv.tv_sec = timeout;
    tv.tv_usec = 0;
    
    FD_ZERO(&fds);
    FD_SET(fd, &fds);
    
    if (select(fd + 1, (fd_set *) 0, &fds, (fd_set *) 0, &tv) == -1)
      return -1;
    
    if (FD_ISSET(fd, &fds)) 
#ifdef WIN32
      c = send(fd, b+sent, blen-sent, 0);
#else
      c = write(fd, b+sent, blen-sent);
#endif
    else
      return -1;

    if (c <= 0) return -1;
    sent += c;
  }

  if (sent != blen) return -1;
  return sent;
}

static int connect_socket(rlm_jradius_t *inst, jradius_socket_t *jrsock)
{
  struct sockaddr_in local_addr, serv_addr;
  int i, connected = 0;
  char buff[128];
  int sock;

  /*
   *     Connect to jradius servers until we succeed or die trying
   */
  for (i = 0; !connected && i < MAX_HOSTS && inst->ipaddr[i] > 0; i++) {

    /*
     *     Allocate a TCP socket
     */
    if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
      ERROR(LOG_PREFIX "could not allocate TCP socket");
      goto failed;
    }
    
    /*
     *     If we have a timeout value set, make the socket non-blocking
     */
    if (inst->timeout > 0 && fcntl(sock, F_SETFL, fcntl(sock, F_GETFL, 0) | O_NONBLOCK) == -1) {
      ERROR(LOG_PREFIX "could not set non-blocking on socket");
      goto failed;
    }
    
    /*
     *     Bind to any local port
     */
    memset(&local_addr, 0, sizeof(local_addr));
    local_addr.sin_family = AF_INET;
    local_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    local_addr.sin_port = htons(0);
    
    if (bind(sock, (struct sockaddr *) &local_addr, sizeof(local_addr)) < 0) {
      ERROR(LOG_PREFIX "could not locally bind TCP socket");
      goto failed;
    }
    
    /*
     *     Attempt connection to remote server
     */
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    memcpy((char *) &serv_addr.sin_addr, &(inst->ipaddr[i]), 4);
    serv_addr.sin_port = htons(inst->port[i]);
    
    if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0) {
      if (inst->timeout > 0 && (errno == EINPROGRESS || errno == EWOULDBLOCK)) {
        /*
        *     Wait to see if non-blocking socket connects or times-out
        */
        struct pollfd pfd;
        memset(&pfd, 0, sizeof(pfd));

        pfd.fd = sock;
        pfd.events = POLLOUT;

        if (poll(&pfd, 1, inst->timeout * 1000) == 1 && pfd.revents) {
          /*
          *     Lets make absolutely sure we are connected
          */
          struct sockaddr_in sa;
          unsigned int salen = sizeof(sa);
          if (getpeername(sock, (struct sockaddr *) &sa, &salen) != -1) {
            /*
            *     CONNECTED! break out of for-loop
            */
            connected = 1;
            break;
          }
        }
      }

      /*
       *     Timed-out
       */
      ERROR(LOG_PREFIX "could not connect to %s:%d", ip_ntoa(buff, inst->ipaddr[i]), inst->port[i]);

    } else {
      /*
       *     CONNECTED (instantly)! break out of for-loop
       */
      connected = 1;
      break;
    }

    /*
     *     Unable to connect, cleanup and start over
     */
    close(sock); sock=0;
  }

  if (!connected) {
    ERROR(LOG_PREFIX "could not find any jradius server!");
    goto failed;
  }

  /*
   *     If we previously set the socket to non-blocking, restore blocking 
  if (inst->timeout > 0 &&
      fcntl(sock, F_SETFL, fcntl(sock, F_GETFL, 0) & ~O_NONBLOCK) == -1) {
    radlog(L_ERR, LOG_PREFIX "could not set blocking on socket");
    goto failed;
  }
   */  

  jrsock->state = is_connected;
  jrsock->con.sock = sock;
  return 1;

 failed:
  if (sock > 0) { shutdown(sock, 2); close(sock); }
  jrsock->state = not_connected;
  return 0;  
}

static inline void close_socket(UNUSED rlm_jradius_t *inst, jradius_socket_t *jrsock)
{
  INFO(LOG_PREFIX "Closing JRadius connection %d", jrsock->id);
  if (jrsock->con.sock > 0) { 
    shutdown(jrsock->con.sock, 2); 
    close(jrsock->con.sock); 
  }
  jrsock->state = not_connected;
  jrsock->con.sock = 0;
}

static inline void free_socket(rlm_jradius_t *inst, jradius_socket_t *jrsock) {
  close_socket(inst, jrsock);
  if (inst->keepalive) {
#ifdef HAVE_PTHREAD_H
    pthread_mutex_destroy(&jrsock->mutex);
#endif
    free(jrsock);
  }
}

static int init_socketpool(rlm_jradius_t *inst)
{
  uint32_t i;
  int rcode, success = 0;
  jradius_socket_t *jrsock;
  
  inst->connect_after = 0;
  inst->socket_pool = NULL;
  
  for (i = 0; i < inst->socket_cnt; i++) {
    INFO(LOG_PREFIX "starting JRadius connection %d", i);
    
    if ((jrsock = rad_malloc(sizeof(*jrsock))) == 0) return -1;
    
    memset(jrsock, 0, sizeof(*jrsock));
    jrsock->id = i;
    jrsock->state = not_connected;

#ifdef HAVE_PTHREAD_H
    rcode = pthread_mutex_init(&jrsock->mutex,NULL);
    if (rcode != 0) {
      ERROR(LOG_PREFIX "Failed to init lock: %s", strerror(errno));
      return 0;
    }
#endif

    if (time(NULL) > inst->connect_after)
      if (connect_socket(inst, jrsock))
	      success = 1;
    
    jrsock->next = inst->socket_pool;
    inst->socket_pool = jrsock;
  }
  inst->last_socket_used = NULL;
  
  if (!success) {
    DEBUG(LOG_PREFIX "Failed to connect to JRadius server.");
  }
  
  return 1;
}

static void free_socketpool(rlm_jradius_t *inst)
{
  jradius_socket_t *cur;
  jradius_socket_t *next;

  for (cur = inst->socket_pool; cur; cur = next) {
    next = cur->next;
    free_socket(inst, cur);
  }
  
  inst->socket_pool = NULL;
}

static jradius_socket_t* get_socket(rlm_jradius_t *inst)
{
  jradius_socket_t *cur, *start;
  int tried_to_connect = 0;
  int unconnected = 0;

  start = inst->last_socket_used;
  if (!start) start = inst->socket_pool;
  
  cur = start;
  
  while (cur) {
#ifdef HAVE_PTHREAD_H
    if (pthread_mutex_trylock(&cur->mutex) != 0) {
      goto next;
    } 
#endif
    
    if ((cur->state == not_connected) && (time(NULL) > inst->connect_after)) {
      INFO(LOG_PREFIX "Trying to (re)connect unconnected handle %d", cur->id);
      tried_to_connect++;
      connect_socket(inst, cur);
    }
    
    if (cur->state == not_connected) {
      DEBUG(LOG_PREFIX "Ignoring unconnected handle %d", cur->id);
      unconnected++;
#ifdef HAVE_PTHREAD_H
      pthread_mutex_unlock(&cur->mutex);
#endif
      goto next;
    }
    
    DEBUG(LOG_PREFIX "Reserving JRadius socket id: %d", cur->id);
    
    if (unconnected != 0 || tried_to_connect != 0) {
      INFO(LOG_PREFIX "got socket %d after skipping %d unconnected handles, tried to reconnect %d though", 
           cur->id, unconnected, tried_to_connect);
    }

    inst->last_socket_used = cur->next;
    return cur;
    
  next:
    cur = cur->next;
    if (!cur) cur = inst->socket_pool;
    if (cur == start) break;
  }
  
  INFO(LOG_PREFIX "There are no sockets to use! skipped %d, tried to connect %d", unconnected, tried_to_connect);
  return NULL;
}

static inline int release_socket(UNUSED rlm_jradius_t *inst, jradius_socket_t *jrsock)
{
#ifdef HAVE_PTHREAD_H
  pthread_mutex_unlock(&jrsock->mutex);
#endif
  
  DEBUG(LOG_PREFIX "Released JRadius socket id: %d", jrsock->id);  
  return 0;
}

/*
 *     Initialize the jradius module
 */
static int mod_instantiate(UNUSED CONF_SECTION *conf, void *instance)
{
  rlm_jradius_t *inst = (rlm_jradius_t *) instance;
  char host[128], b[128];
  const char *h;
  int i, p, idx, port;

  for (i = 0, idx = 0; i < MAX_HOSTS; i++) {
    if (inst->host[i] && strlen(inst->host[i]) < sizeof(host)) {
      h = inst->host[i];
      p = JRADIUS_PORT;
      
      strcpy(b, h);
      if (sscanf(b, "%[^:]:%d", host, &port) == 2) { h = host; p = port; }

      if (h) {
	      fr_ipaddr_t ipaddr;

        if (ip_hton(&ipaddr, AF_INET, h, false) < 0) {  
          ERROR(LOG_PREFIX "can't find IP address for host %s", h);
          continue;
        }
        
        if ((inst->ipaddr[idx] = ipaddr.ipaddr.ip4addr.s_addr) != htonl(INADDR_NONE)) {
          inst->port[idx] = p;
          INFO(LOG_PREFIX "configuring jradius server %s:%d", h, p);
          idx++;
        } else {
          ERROR(LOG_PREFIX "invalid jradius server %s", h);
        }
      }
    }
  }

  if (inst->keepalive) init_socketpool(inst);

  inst->onfail = RLM_MODULE_FAIL;

  if (inst->onfail_s) {
    if      (!strcmp(inst->onfail_s, "NOOP"))    inst->onfail = RLM_MODULE_NOOP;
    else if (!strcmp(inst->onfail_s, "REJECT"))  inst->onfail = RLM_MODULE_REJECT;
    else if (!strcmp(inst->onfail_s, "OK"))      inst->onfail = RLM_MODULE_OK;
    else if (!strcmp(inst->onfail_s, "FAIL"))    inst->onfail = RLM_MODULE_FAIL;
    else ERROR(LOG_PREFIX "invalid jradius 'onfail' state %s", inst->onfail_s);
  }

  return 0;
}

/*
 *     Initialize a byte array buffer structure
 */
static inline void init_byte_array(byte_array_t *ba, unsigned char *b, int blen)
{
  ba->b = b;
  ba->size = ba->left = blen;
  ba->pos = 0;
}

/*
 *     Pack a single byte into a byte array buffer
 */
static inline int pack_byte(byte_array_t *ba, unsigned char c)
{
  if (ba->left < 1) return -1;

  ba->b[ba->pos] = c;
  ba->pos++;
  ba->left--;

  return 0;
}

/*
 *     Pack an array of bytes into a byte array buffer
 */
static inline int pack_bytes(byte_array_t *ba, unsigned char *d, unsigned int dlen)
{
  if (ba->left < dlen) return -1;

  memcpy((void *)(ba->b + ba->pos), d, dlen);
  ba->pos  += dlen;
  ba->left -= dlen;

  return 0;
}

/*
 *     Pack an integer64 into a byte array buffer (adjusting for byte-order)
 */
static inline int pack_uint64(byte_array_t *ba, uint64_t i)    
{
  if (ba->left < 8) return -1;

  i = htonll(i);

  memcpy((void *)(ba->b + ba->pos), (void *)&i, 8);
  ba->pos  += 8;
  ba->left -= 8;

  return 0;
}

/*
 *     Pack an integer into a byte array buffer (adjusting for byte-order)
 */
static inline int pack_uint32(byte_array_t *ba, uint32_t i)
{
  if (ba->left < 4) return -1;

  i = htonl(i);

  memcpy((void *)(ba->b + ba->pos), (void *)&i, 4);
  ba->pos  += 4;
  ba->left -= 4;

  return 0;
}

/*
 *     Pack a short into a byte array buffer (adjusting for byte-order)
 */
static inline int pack_uint16(byte_array_t *ba, uint16_t i)
{
  if (ba->left < 2) return -1;

  i = htons(i);

  memcpy((void *)(ba->b + ba->pos), (void *)&i, 2);
  ba->pos  += 2;
  ba->left -= 2;

  return 0;
}

/*
 *     Pack a byte into a byte array buffer 
 */
static inline int pack_uint8(byte_array_t *ba, uint8_t i)
{
  if (ba->left < 1) return -1;

  memcpy((void *)(ba->b + ba->pos), (void *)&i, 1);
  ba->pos  += 1;
  ba->left -= 1;

  return 0;
}

/*
 *     Pack one byte array buffer into another byte array buffer
 */
static inline int pack_array(byte_array_t *ba, byte_array_t * a)
{
  if (ba->left < a->pos) return -1;

  memcpy((void *)(ba->b + ba->pos), (void *)a->b, a->pos);
  ba->pos  += a->pos;
  ba->left -= a->pos;

  return 0;
}

/*
 *     Pack radius attributes into a byte array buffer
 */
static inline int pack_vps(byte_array_t *ba, VALUE_PAIR *vps)
{
  uint32_t i;
  VALUE_PAIR *vp;
  DICT_ATTR const *da;

  for (vp = vps; vp != NULL; vp = vp->next) {
    da = vp->da;
    
    DEBUG(LOG_PREFIX "packing attribute %s (vendor: %d, id: %d; len: %zu)", da->name, da->vendor, da->attr, vp->vp_length);

    i = ATTRIBUTE_TYPE(da->vendor, da->attr);
    if (pack_uint32(ba, i) == -1) return -1;

    i = vp->vp_length;
    if (pack_uint32(ba, i) == -1) return -1;

    i = vp->op;
    if (pack_uint32(ba, i) == -1) return -1;

    switch (da->type) {
      case PW_TYPE_BYTE:
	      if (pack_uint8(ba, vp->vp_byte) == -1) return -1;
	      break;

      case PW_TYPE_SHORT:
        if (pack_uint16(ba, vp->vp_short) == -1) return -1;
        break;

      case PW_TYPE_INTEGER:
      case PW_TYPE_DATE:  
        if (pack_uint32(ba, vp->vp_integer) == -1) return -1;
        break;

      case PW_TYPE_SIGNED:    
        if (pack_uint32(ba, vp->vp_signed) == -1) return -1;
        break;

      case PW_TYPE_INTEGER64:  
        if (pack_uint64(ba, vp->vp_integer64) == -1) return -1;
        break;

      case PW_TYPE_ETHERNET:
        if (pack_bytes(ba, (void*) vp->vp_ether, vp->length) == -1) return -1;
        break;

      case PW_TYPE_IPV4_ADDR:  
        if (pack_bytes(ba, (void*) &vp->vp_ipaddr, vp->length) == -1) return -1;
        break;

      case PW_TYPE_IPV6_ADDR:
        if (pack_bytes(ba, (void*) &vp->vp_ipv6addr, vp->length) == -1) return -1;
        break;

      case PW_TYPE_IPV4_PREFIX:
        if (pack_bytes(ba, (void*) vp->vp_ipv4prefix, vp->length) == -1) return -1;
        break;

      case PW_TYPE_IPV6_PREFIX:
        if (pack_bytes(ba, (void*) vp->vp_ipv6prefix, vp->length) == -1) return -1;
        break;    

      case PW_TYPE_IFID:
        if (pack_bytes(ba, (void*) vp->vp_ifid, vp->length) == -1) return -1;
        break;

      case PW_TYPE_ABINARY:
        if (pack_bytes(ba, (void*) vp->vp_filter, vp->length) == -1) return -1;
        break;

      case PW_TYPE_STRING:
        if (pack_bytes(ba, (void*) vp->vp_strvalue, vp->length) == -1) return -1;
        break;

      default:          
        if (pack_bytes(ba, (void*) vp->vp_octets, vp->length) == -1) return -1;
        break;
    }
  }

  return 0;
}

/*
 *     Pack a radius packet into a byte array buffer
 */
static inline int pack_packet(byte_array_t *ba, RADIUS_PACKET *p)
{
  unsigned char buff[HALF_MESSAGE_LEN];
  byte_array_t pba;

  init_byte_array(&pba, buff, sizeof(buff));

  if (pack_vps(&pba, p->vps) == -1) return -1;

  DEBUG(LOG_PREFIX "packing packet with code: %d (attr length: %d)", p->code, pba.pos);

#ifdef LEGACY_FMT
  if (pack_byte(ba, p->code) == -1) return -1;
  if (pack_byte(ba, p->id) == -1) return -1;
#else
  if (pack_uint32(ba, p->code) == -1) return -1;
  if (pack_uint32(ba, p->id) == -1) return -1;
#endif
  if (pack_uint32(ba, pba.pos) == -1) return -1;

  if (pba.pos == 0) return 0;
  if (pack_array(ba, &pba) == -1) return -1;

  return 0;
}

static inline int pack_request(byte_array_t *ba, REQUEST *r)
{
  unsigned char buff[HALF_MESSAGE_LEN];
  byte_array_t pba;

  init_byte_array(&pba, buff, sizeof(buff));

  if (pack_vps(&pba, r->config) == -1) return -1;
  if (pack_uint32(ba, pba.pos) == -1) return -1;
  if (pba.pos == 0) return 0;
  if (pack_array(ba, &pba) == -1) return -1;
      
  return 0;
}

static inline uint64_t unpack_uint64(unsigned char *c)
{
  uint64_t ii;
  memcpy((void *)&ii, c, 8);
  return ntohll(ii);
}

static inline uint32_t unpack_uint32(unsigned char *c)
{
  uint32_t ii;
  memcpy((void *)&ii, c, 4);
  return ntohl(ii);
}

static inline uint16_t unpack_uint16(unsigned char *c)
{
  uint16_t ii;
  memcpy((void *)&ii, c, 2);
  return ntohs(ii);
}

static inline uint8_t unpack_uint8(unsigned char *c)
{
  uint8_t ii;
  memcpy((void *)&ii, c, 1);
  return ii;
}

/*
 *     Read a single byte from socket
 */
static inline int read_byte(rlm_jradius_t *inst, jradius_socket_t *jrsock, uint8_t *b)
{
  return (sock_read(inst, jrsock, b, 1) == 1) ? 0 : -1;
}

/*
 *     Read an integer from the socket (adjusting for byte-order)
 */
static inline int read_uint32(rlm_jradius_t *inst, jradius_socket_t *jrsock, uint32_t *i)
{
  uint32_t ii;

  if (sock_read(inst, jrsock, (uint8_t *)&ii, 4) != 4) return -1;
  *i = ntohl(ii);

  return 0;
}

/*
 *     Read a value-pair list from the socket
 */
static inline int read_vps(rlm_jradius_t *inst, jradius_socket_t *jrsock, void *ctx, VALUE_PAIR **vps, int vps_len)
{
  VALUE_PAIR *vp;
  DICT_ATTR *da;

  unsigned char buff[MESSAGE_LEN];
  uint32_t alen, atype, aop;
  int rlen = 0;
  
  while (rlen < vps_len) {
    if (read_uint32(inst, jrsock, &atype) == -1) return -1; rlen += 4;
    if (read_uint32(inst, jrsock, &alen)  == -1) return -1; rlen += 4;
    if (read_uint32(inst, jrsock, &aop)   == -1) return -1; rlen += 4; 

    DEBUG(LOG_PREFIX "reading attribute: type=%d; len=%d", atype, alen);

    if (alen >= sizeof(buff)) {
      ERROR(LOG_PREFIX "packet value too large (len: %d)", alen);
      return -1;
    }

    if (sock_read(inst, jrsock, buff, alen) != (int) alen) return -1; rlen += alen;
    buff[alen]=0;

    /*
     *     Create new attribute
     */
    vp = fr_pair_afrom_num(ctx, ATTRIBUTE_ID(atype), VENDOR_ID(atype)); 
    DEBUG(LOG_PREFIX "attribute from dict: vendor=%d; id=%d", vp->da->vendor, vp->da->attr);

		if (!vp) {
      ERROR(LOG_PREFIX "could not allocate attribute");
			return -1;
		}    
    
    vp->op = aop;
    vp->vp_length = alen; 

    da = vp->da;

    if (da->type == PW_TYPE_INVALID) {
      /*
       *     FreeRADIUS should know about the same attributes that JRadius knows
       */
      ERROR(LOG_PREFIX "received attribute we do not recognize (type: %d)", atype);
      fr_pair_list_free(&vp);
      continue;
    }

    /* 
     *     WiMAX combo-ip address
     *     paircreate() cannot recognize the real type of the address.
     *     ..ugly code...
     */
    if (da->type == PW_TYPE_COMBO_IP_ADDR) {
        switch (vp->vp_length) {
            case 4:
                da->type = PW_TYPE_IPV4_ADDR;
                break;
            case 16:
                da->type = PW_TYPE_IPV6_ADDR;
                break;
        }
    }

    /*
     *     Fill in the attribute value based on type
     */
    switch (da->type) { 
      case PW_TYPE_BYTE: 
        if (vp->vp_length != 1) goto invalid_avp;
        vp->vp_byte = unpack_uint8(buff);
        break;

      case PW_TYPE_SHORT: 
        if (vp->vp_length != 2) goto invalid_avp;
        vp->vp_short = unpack_uint16(buff);
        break;

      case PW_TYPE_INTEGER: 
      case PW_TYPE_DATE:
        if (vp->vp_length != 4) goto invalid_avp;
        vp->vp_integer = unpack_uint32(buff);
        break;

      case PW_TYPE_SIGNED:
        if (vp->vp_length != 4) goto invalid_avp;
        vp->vp_signed = unpack_uint32(buff);
        break;

      case PW_TYPE_INTEGER64:
        if (vp->vp_length != 8) goto invalid_avp;
		    vp->vp_integer64 = unpack_uint64(buff);   
        break;   

      case PW_TYPE_ETHERNET:  
        if (vp->vp_length != 6) goto invalid_avp;
        memcpy(vp->vp_ether, buff, vp->vp_length);
        break;

      case PW_TYPE_IPV4_ADDR:   
        if (vp->vp_length != 4) goto invalid_avp;
        memcpy(&vp->vp_ipaddr, buff, vp->vp_length);
        break;

      case PW_TYPE_IPV6_ADDR:   
        if (vp->vp_length != 16) goto invalid_avp;
        memcpy(&vp->vp_ipv6addr, buff, vp->vp_length);
        break;        

      case PW_TYPE_IPV4_PREFIX:   
        if (vp->vp_length > 6) goto invalid_avp;
        memcpy(vp->vp_ipv4prefix, buff, vp->vp_length);
        break;        

      case PW_TYPE_IPV6_PREFIX:   
        if (vp->vp_length > 18) goto invalid_avp;
        memcpy(vp->vp_ipv6prefix, buff, vp->vp_length);
        break;   

      case PW_TYPE_IFID:      
        if (vp->vp_length != 8) goto invalid_avp;
        memcpy(vp->vp_ifid, buff, vp->vp_length);
        break;     

      case PW_TYPE_ABINARY:
        if (vp->vp_length > sizeof(vp->vp_filter)) {          
            vp->vp_length = sizeof(vp->vp_filter);
        }
        memcpy(vp->vp_filter, buff, vp->vp_length);      
        break;

      case PW_TYPE_STRING:
        fr_pair_value_bstrncpy(vp, buff, vp->vp_length);
        break;

      default:
        fr_pair_value_memcpy(vp, buff, vp->vp_length);
        break;
        
      invalid_avp:
        ERROR(LOG_PREFIX "ignoring received attribute with invalid length: type=%d; len=%d", da->type, vp->vp_length);
        fr_pair_list_free(&vp);
        continue;
      }
  
      /*
      *     Add the attribute to the packet
      */
      fr_pair_add(vps, vp);
  } 

  return rlen;
}

/*
 *     Read a radius packet from the socket
 */
static inline int read_packet(rlm_jradius_t * inst, jradius_socket_t *jrsock, RADIUS_PACKET *p)
{
  uint32_t code;
  uint32_t id;
  uint32_t plen;

#ifdef LEGACY_FMT
  { 
    uint8_t c = 0;
    if (read_byte(inst, jrsock, &c) == -1) return -1;
    code = c;
    
    if (read_byte(inst, jrsock, &c) == -1) return -1;
    id = c; 
  }
#else
  if (read_uint32(inst, jrsock, &code) == -1) return -1;
  if (read_uint32(inst, jrsock, &id)   == -1) return -1;
#endif

  if (read_uint32(inst, jrsock, &plen) == -1) return -1;

  DEBUG(LOG_PREFIX "reading packet: code=%d len=%d", (int)code, plen);

  if (inst->allow_codechange)
    if (code != p->code) {
      INFO(LOG_PREFIX "changing packet code from %d to %d", p->code, code);
      p->code = code;
    }

  if (inst->allow_idchange)
    if ((int)id != p->id) {
      INFO(LOG_PREFIX "changing packet id from %d to %d", p->id, id);
      p->id = (int)id;
    }
  
  /*
   *     Delete previous attribute list
   */
  fr_pair_list_free(&p->vps);

  if (plen == 0) return 0;

  if (read_vps(inst, jrsock, p, &p->vps, plen) == -1) return -1;

  return 0;
}

static inline int read_request(rlm_jradius_t *inst, jradius_socket_t *jrsock, REQUEST *p)
{
  unsigned int plen;

  if (read_uint32(inst, jrsock, &plen) == -1) return -1;

  DEBUG(LOG_PREFIX "reading request: config_item: len=%d", plen);

  /*
   *     Delete previous attribute list
   */
  fr_pair_list_free(&p->config);

  if (plen == 0) return 0;

  if (read_vps(inst, jrsock, p, &p->config, plen) == -1) return -1;

  return 0;
}

static int mod_jradius_call(char func, void *instance, REQUEST *req, int isproxy)
{
  rlm_jradius_t *inst = instance;

  RADIUS_PACKET *request = isproxy ? req->proxy : req->packet;
  RADIUS_PACKET *reply = isproxy ? req->proxy_reply : req->reply;

  jradius_socket_t *jrsock  = 0;
  jradius_socket_t sjrsock;

  int exitstatus = inst->onfail;
  unsigned char rcode, pcount;

  unsigned char buff[MESSAGE_LEN];
  byte_array_t ba;

  char const *n = inst->name;
  unsigned int nlen = strlen(n);
  const char *err = 0;
  int rc, attempt2=0;

  uint32_t len = 0;

#define W_ERR(s) { err=s; goto packerror;  }
#define R_ERR(s) { err=s; goto parseerror; }

  if (inst->keepalive) {
    jrsock = get_socket(inst);
    if (!jrsock) return exitstatus;
  } else {
    jrsock = &sjrsock;
    memset(jrsock, 0, sizeof(*jrsock));
    jrsock->state = not_connected;
  }

  init_byte_array(&ba, buff, sizeof(buff));

  pcount = 0;
  if (request) pcount++;
  if (reply) pcount++;

  /*
   *     Create byte array to send to jradius
   */
  if ((rc = pack_uint32 (&ba, 0))                     == -1)  W_ERR("pack_uint32(0)");  
  if ((rc = pack_uint32 (&ba, nlen))                  == -1)  W_ERR("pack_uint32(nlen)");
  if ((rc = pack_bytes  (&ba, (void *)n, nlen))       == -1)  W_ERR("pack_bytes(name)");
  if ((rc = pack_byte   (&ba, func))                  == -1)  W_ERR("pack_byte(fun)");
  if ((rc = pack_byte   (&ba, pcount))                == -1)  W_ERR("pack_byte(pcnt)");
  if (pcount > 0 && (rc = pack_packet (&ba, request)) == -1)  W_ERR("pack_packet(req)");
  if (pcount > 1 && (rc = pack_packet (&ba, reply))   == -1)  W_ERR("pack_packet(rep)");
  if ((rc = pack_request(&ba, req))                   == -1)  W_ERR("pack_request()");

  /*
   *     Send data
   */
 start_over:
  if (jrsock->state == not_connected) {
    if (attempt2) ERROR(LOG_PREFIX "reconnecting socket id %d", jrsock->id);
    if (!connect_socket(inst, jrsock)) {
      if (attempt2) ERROR(LOG_PREFIX "could not reconnect socket %d, giving up", jrsock->id);
      goto cleanup;
    }
  }

  DEBUG(LOG_PREFIX "sending %d bytes to socket %d", ba.pos, jrsock->id);

  /*
   * Set the overall request length
   */
  len = htonl(ba.pos);
  memcpy((void*) ba.b, (void*) &len, 4);

  len = 0;
  jrsock->ba = 0;

  if (sock_write(inst, jrsock, ba.b, ba.pos) != (int)ba.pos || (rc = read_uint32(inst, jrsock, &len)) == -1) {
    /*
     *   With an error on the write or the first read, try closing the socket
     *   and reconnecting to see if that improves matters any (tries this only once)
     */
    ERROR(LOG_PREFIX "error sending request with socket %d", jrsock->id);
    if (!inst->keepalive || attempt2) W_ERR("socket_send/first_read");
    close_socket(inst, jrsock);
    attempt2 = 1;
    goto start_over;
  }

  /*
   * Reset buffer
   */

  if (len == 0) 
    goto parseerror;
  
  if (len > sizeof(buff))
    goto parseerror;
  
  if (sock_read(inst, jrsock, buff, len) != len)
    goto parseerror;
  
  DEBUG(LOG_PREFIX "read %d bytes at once", (int) len);
  
  init_byte_array(&ba, buff, sizeof(buff));
  
  jrsock->ba = &ba;

  /*
   *     Read result
   */
  if ((rc = read_byte(inst, jrsock, &rcode))  == -1)  R_ERR("read_byte(rcode)");    
  if ((rc = read_byte(inst, jrsock, &pcount)) == -1)  R_ERR("read_byte(pcnt)");

  DEBUG(LOG_PREFIX "return code %d; receiving %d packets", (int)rcode, (int)pcount);

  if (pcount > 0 && request) if ((rc = read_packet (inst, jrsock, request)) == -1)  R_ERR("read_packet(req)");
  if (pcount > 1 && reply)   if ((rc = read_packet (inst, jrsock, reply))   == -1)  R_ERR("read_packet(rep)");

  if ((rc = read_request(inst, jrsock, req)) == -1) R_ERR("read_request()");

  /*
   *    Since we deleted all the attribute lists in the request,
   *    we need to reconfigure a few pointers in the REQUEST object
   */
  if (req->username) {
    req->username = fr_pair_find_by_num(request->vps, PW_USER_NAME, 0, TAG_ANY);
  }
  if (req->password) {
    req->password = fr_pair_find_by_num(request->vps, PW_USER_PASSWORD, 0, TAG_ANY);
    if (!req->password) req->password = fr_pair_find_by_num(request->vps, PW_CHAP_PASSWORD, 0, TAG_ANY);
  }
  
  /*
   *    All done, set return code and cleanup
   */
  exitstatus = (int)rcode;
  goto cleanup;

 parseerror:
  ERROR(LOG_PREFIX "problem parsing the data [%s]",err);
  if (inst->keepalive) close_socket(inst, jrsock);
  goto cleanup;

 packerror:
  ERROR(LOG_PREFIX "problem packing the data[%s]",err);
  if (inst->keepalive) close_socket(inst, jrsock);

 cleanup:
  if (inst->keepalive) 
    release_socket(inst, jrsock);
  else  
    close_socket(inst, jrsock);

  return exitstatus;
}

static rlm_rcode_t CC_HINT(nonnull) mod_authenticate(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_authenticate, instance, request, 0);
}

static rlm_rcode_t CC_HINT(nonnull) mod_authorize(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_authorize, instance, request, 0);
}

static rlm_rcode_t CC_HINT(nonnull) mod_preacct(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_preacct, instance, request, 0);
}

static rlm_rcode_t CC_HINT(nonnull) mod_accounting(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_accounting, instance, request, 0);
}

static rlm_rcode_t CC_HINT(nonnull) mod_checksimul(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_checksimul, instance, request, 0);
}

static rlm_rcode_t CC_HINT(nonnull) mod_pre_proxy(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_pre_proxy, instance, request, 1);
}

static rlm_rcode_t CC_HINT(nonnull) mod_post_proxy(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_post_proxy, instance, request, 1);
}

static rlm_rcode_t CC_HINT(nonnull) mod_post_auth(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_post_auth, instance, request, 0);
}

#ifdef WITH_COA
static rlm_rcode_t CC_HINT(nonnull) mod_recv_coa(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_recv_coa, instance, request, 0);
}
static rlm_rcode_t CC_HINT(nonnull) mod_send_coa(void *instance, REQUEST *request)
{
  return mod_jradius_call(JRADIUS_send_coa, instance, request, 0);
}
#endif

static int mod_detach(void *instance)
{
  rlm_jradius_t *inst = (rlm_jradius_t *) instance;
  free_socketpool(inst);
  free(inst);
  return 0;
}

extern module_t rlm_jradius;
module_t rlm_jradius = {
	.magic		= RLM_MODULE_INIT,
	.name		= "jradius",
	.type		= RLM_TYPE_THREAD_SAFE,
  .inst_size	= sizeof(rlm_jradius_t),
	.config		= module_config,
	.instantiate	= mod_instantiate,
	.detach		= mod_detach,
	.methods = {
		[MOD_AUTHENTICATE] = mod_authenticate,
		[MOD_AUTHORIZE]	= mod_authorize,
		[MOD_PREACCT]	= mod_preacct,
		[MOD_ACCOUNTING] = mod_accounting,
		[MOD_SESSION]	= mod_checksimul,
    [MOD_PRE_PROXY] = mod_pre_proxy,
    [MOD_POST_PROXY] = mod_post_proxy,
    [MOD_POST_AUTH] = mod_post_auth,
#ifdef WITH_COA
    [MOD_RECV_COA] = mod_recv_coa,
    [MOD_SEND_COA] = mod_send_coa
#endif  
  }
};

