package net.jradius.ewt.handler;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.VSADictionary;

import org.springframework.beans.factory.InitializingBean;

public class AttributeSearchTree implements Map<String, Class>, InitializingBean
{
    private AttributeDictionary attributeDictionary;
    private Node _root;

    public AttributeSearchTree()
    {
    }
    
    public void afterPropertiesSet() throws Exception
    {
        attributeDictionary.loadAttributesNames(this);
        
        LinkedHashMap<Long, Class> vendorMap = new LinkedHashMap<Long, Class>();
        attributeDictionary.loadVendorCodes(vendorMap);

        for (Long id : vendorMap.keySet())
        {
            Class c = vendorMap.get(id);
            try
            {
                VSADictionary vsadict = (VSADictionary)c.newInstance();
                vsadict.loadAttributesNames(this);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    public void clear()
    {
    }

    public boolean containsKey(Object key)
    {
        return false;
    }

    public boolean containsValue(Object value)
    {
        return false;
    }

    public Set<Entry<String, Class>> entrySet()
    {
        return null;
    }

    public Class get(Object key)
    {
        return null;
    }

    public boolean isEmpty()
    {
        return false;
    }

    public Set<String> keySet()
    {
        return null;
    }

    public Class put(String key, Class value)
    {
        add(key);
        return null;
    }

    public void putAll(Map<? extends String, ? extends Class> m)
    {
    }

    public Class remove(Object key)
    {
        return null;
    }

    public int size()
    {
        return 0;
    }

    public Collection<Class> values()
    {
        return null;
    }

    public void add(String word) 
    {
        Node node = insert(_root, word, word.toLowerCase(), 0);
        if (_root == null) 
        {
            _root = node;
        }
    }

    public boolean contains(String word) 
    {
        Node node = search(_root, word.toLowerCase(), 0);
        return node != null && node.isEndOfWord();
    }

    public void prefixSearch(String prefix, List<String> results, int limit) 
    {
        if (prefix == null) return;
        inOrderTraversal(search(_root, prefix.toLowerCase(), 0), results, limit);
    }

    private Node search(Node node, CharSequence word, int index) 
    {
        Node result = node;

        if (node == null) 
        {
            return null;
        }

        char c = word.charAt(index);

        if (c == node.getChar())
        {
            if (index + 1 < word.length()) 
            {
                result = search(node.getChild(), word, index + 1);
            }
            else
            {
                result = node.getChild();
            }
        } 
        else if (c < node.getChar())
        {
            result = search(node.getSmaller(), word, index);
        } 
        else 
        {
            result = search(node.getLarger(), word, index);
        }

        return result;
    }

    private Node insert(Node node, CharSequence word, CharSequence wordLower, int index) 
    {
        char c = wordLower.charAt(index);

        if (node == null) 
        {
            return insert(new Node(c), word, wordLower, index);
        }

        if (c == node.getChar()) 
        {
            if (index + 1 < word.length()) 
            {
                node.setChild(insert(node.getChild(), word, wordLower, index + 1));
            } 
            else 
            {
                node.setWord(word.toString());
            }
        } 
        else if (c < node.getChar()) 
        {
            node.setSmaller(insert(node.getSmaller(), word, wordLower, index));
        } 
        else 
        {
            node.setLarger(insert(node.getLarger(), word, wordLower, index));
        }

        return node;
    }

    private void inOrderTraversal(Node node, List<String> results, int limit)
    {
        if (node == null) 
        {
            return;
        }

        inOrderTraversal(node.getSmaller(), results, limit);
        if (node.isEndOfWord()) 
        {
            results.add(node.getWord());
        }
        inOrderTraversal(node.getChild(), results, limit);
        inOrderTraversal(node.getLarger(), results, limit);
    }

    private static final class Node
    {
        private final char _c;
        private Node _smaller;
        private Node _larger;
        private Node _child;
        private String _word;

        public Node(char c) 
        {
            _c = c;
        }

        public char getChar() 
        {
            return _c;
        }

        public Node getSmaller() 
        {
            return _smaller;
        }

        public void setSmaller(Node smaller)
        {
            _smaller = smaller;
        }

        public Node getLarger()
        {
            return _larger;
        }

        public void setLarger(Node larger) 
        {
            _larger = larger;
        }

        public Node getChild() 
        {
            return _child;
        }

        public void setChild(Node child) 
        {
            _child = child;
        }

        public String getWord() 
        {
            return _word;
        }

        public void setWord(String word) 
        {
            _word = word;
        }

        public boolean isEndOfWord() 
        {
            return getWord() != null;
        }
    }

    public AttributeDictionary getAttributeDictionary()
    {
        return attributeDictionary;
    }

    public void setAttributeDictionary(AttributeDictionary attributeDictionary)
    {
        this.attributeDictionary = attributeDictionary;
    }
}
