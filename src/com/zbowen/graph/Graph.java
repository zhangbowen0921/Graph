package com.zbowen.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Graph<V, E> {
	
	protected WeightManager<E> weightManager;
	
	public Graph() {}
	
	public Graph(WeightManager<E> weightManager) {
		this.weightManager = weightManager;
	}

	public abstract List<V> topologicalSortBfs(); //拓扑排序 广度优先
	
	public abstract List<V> topologicalSortDfs(); //拓扑排序 深度优先
	
	public abstract Set<EdgeInfo<V, E>> mst(); //最小生成树
	
	public abstract Map<V, PathInfo<V, E>> shortestPath(V begin); //单源最短路径
	
	public abstract Map<V, Map<V, PathInfo<V, E>>> shortestPath(); //多源最短路径
	
	abstract int verticesSize(); //顶点数量
	
	abstract int edgesSize(); //边的数量
	
	abstract void addVertex(V v); //添加一个顶点
	
	abstract void removeVertex(V v); //删除一个顶点
	
	abstract void addEdge(V fromV, V toV); //添加一条边
	
	abstract void addEdge(V fromV, V toV, E weight); //添加一条边 带权重
	
	abstract void removeEdge(V fromV, V toV); //删除一条边
	
	public abstract void bfs(V begin, VertexVisitor<V> visitor); //广度优先遍历 利用队列
	
	public abstract void dfs(V begin, VertexVisitor<V> visitor); //深度优先遍历 利用栈 迭代
	
	public abstract void dfsRecursive(V value, VertexVisitor<V> visitor); //深度优先遍历 递归
	
	public interface WeightManager<E> {
		int compare(E w1, E w2);
		E add(E w1, E w2);
		E zero();
	}
	
	public interface VertexVisitor<V> {
		boolean visit(V v);
	}
	
	public static class PathInfo<V, E> {
		protected E weight; //总权值
		//路径 信息
		protected List<EdgeInfo<V, E>> edgeInfos = new LinkedList<>();
		
		public PathInfo() {}
		
		public PathInfo(E weight) {
			this.weight = weight;
		}

		public E getWeight() {
			return weight;
		}
		
		public void setWeight(E weight) {
			this.weight = weight;
		}
		
		public List<EdgeInfo<V, E>> getEdgeInfos() {
			return edgeInfos;
		}
		
		public void setEdgeInfos(List<EdgeInfo<V, E>> edgeInfos) {
			this.edgeInfos = edgeInfos;
		}
	}
	
	public static class EdgeInfo<V, E> {
		private V from;
		private V to;
		private E weight;
		
		public EdgeInfo(V from, V to, E weight) {
			this.from = from;
			this.to = to;
			this.weight = weight;
		}
		
		public V getFrom() {
			return from;
		}
		
		public void setFrom(V from) {
			this.from = from;
		}
		
		public V getTo() {
			return to;
		}
		
		public void setTo(V to) {
			this.to = to;
		}
		
		public E getWeight() {
			return weight;
		}
		
		public void setWeight(E weight) {
			this.weight = weight;
		}

		@Override
		public String toString() {
			return "EdgeInfo [from=" + from + ", to=" + to + ", weight=" + weight + "]";
		}
		
	}

}
