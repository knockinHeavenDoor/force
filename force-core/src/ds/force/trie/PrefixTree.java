package ds.force.trie;

import ds.force.primitive.CharObjectMap;

import java.util.*;

public class PrefixTree implements Trie {

    private transient TrieNode root;

    private static final int SINGLE = 1;

    public PrefixTree(){
        this.root = new TrieNode();
    }

    public PrefixTree(Collection<String> c){
        this.root = new TrieNode();
        for (String word : c) {
            add(word);
        }
    }

    private static class TrieNode {

        private CharObjectMap<TrieNode> childes;

        private boolean isEnd;

        TrieNode() {
            childes = new CharObjectMap<>();
            isEnd = false;
        }

        boolean contains(char character){
            return childes.containsKey(character);
        }

        TrieNode getTrieNode(char character){
            return childes.get(character);
        }

        boolean isEnd(){
            return isEnd;
        }
    }

    public boolean isEmpty(){
        return this.root.childes.isEmpty();
    }

    @Override
    public boolean find(String word) {
        char[] chars = word.toCharArray();
        TrieNode node = this.root;
        for (int i = 0; i < chars.length; i++) {
            char character = chars[i];
            if (node.contains(character)){
                node = node.getTrieNode(character);
            } else {
                return false;
            }
        }
        return node.isEnd();
    }

    @Override
    public boolean add(String word) {
        char[] chars = word.toCharArray();
        TrieNode node = this.root;
        for (int i = 0; i < chars.length; i++) {
            char character = chars[i];
            if (node.contains(character)){
                node = node.getTrieNode(character);
            } else {
                TrieNode newNode = new TrieNode();
                node.childes.put(character,newNode);
                node = newNode;
            }
        }
        node.isEnd = true;
        return true;
    }

    @Override
    public boolean remove(String word) {
        char[] chars = word.toCharArray();
        TrieNode node = this.root;
        Deque<TrieNode> stack = new ArrayDeque<>();
        for (int i = 0; i < chars.length; i++) {
            char character = chars[i];
            stack.push(node);
            if (node.contains(character)){
                node = node.getTrieNode(character);
            } else {
                return false;
            }
        }
        if (!node.isEnd()) return false;
        int index = chars.length - 1;
        while (!stack.isEmpty()){
            TrieNode parent = stack.pop();
            parent.childes.remove(chars[index]);
            if (parent.childes.size() > SINGLE || parent.isEnd()){
                break;
            }
            --index;
        }
        return true;
    }

    public void clear(){
        this.root = new TrieNode();
    }

    public List<String> search(String prefix){
        TrieNode node = searchNode(prefix);
        if (node == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        Deque<TrieNode> stack = new ArrayDeque<>();
        stack.push(node);
        while (!stack.isEmpty()){
            TrieNode child = stack.pop();
            //todo
        }
        return null;
    }

    final TrieNode searchNode(String prefix){
        char[] chars = prefix.toCharArray();
        TrieNode node = this.root;
        for (int i = 0; i < chars.length; i++) {
            char character = chars[i];
            if (node.contains(character)){
                node = node.getTrieNode(character);
            } else {
                return null;
            }
        }
        return node;
    }
}
