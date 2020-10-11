package com.zbowen.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 泛型的并查集 基于rank的优化 + 路径分裂 Path Spliting
 * @author zbowen
 *
 * @param <V> 节点对象的值
 */
public class GenericUnionFind<V> {

	Map<V, Node<V>> nodes = new HashMap<>();
	
	private static class Node<V> {
		V value;
		Node<V> parent;
		int rank;
		
		public Node(V value) {
			this.value = value;
			parent = this;
			this.rank = 1;
		}
		
	}

	public void makeSet(V value) {
		if (nodes.containsKey(value)) return;
		nodes.put(value, new Node<>(value));
	}
	
	/**
	 * 路径分裂 Path Spliting
	 * @param v
	 * @return
	 */
	public V find(V v) {
		Node<V> node = findNode(v);
		return node == null ? null : node.value;
	}
	
	public void union(V v1, V v2) {
		//判断 v1, v2有没有在map集合里面
		Node<V> node1 = findNode(v1);
		Node<V> node2 = findNode(v2);
		if (node1 == null || node2 == null) return;
		if (Objects.equals(node1.value, node2.value)) return;
		
		//合并 基于rank优化 把 高度 小的树 嫁接到 高度大的树上
		if (node1.rank < node2.rank) {
			node1.parent = node2;
		}else if (node1.rank > node2.rank) {
			node2.parent = node1;
		}else {
			node2.parent = node1;
			node1.rank++;
		}
	}

	public boolean isSame(V v1, V v2) {
		V p1 = find(v1);
		V p2 = find(v2);
		if (p1 == null || p2 == null) return false;
		return Objects.equals(p1, p2);
	}
	
	private Node<V> findNode(V v) {
		Node<V> node = nodes.get(v);
		if (node == null) return null;
		while (!Objects.equals(node.parent.value, node.value)) {
			Node<V> parent = node.parent;
			node.parent = parent.parent;
			node = parent;
		}
		return node;
	}

}
