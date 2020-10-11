package com.zbowen.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zbowen.graph.Graph.PathInfo;

import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

@SuppressWarnings("all")
public class ListGraph<V, E> extends Graph<V, E> {

	Map<V, Vertex<V, E>> vertices = new HashMap<>(); //用来存储 顶点
	Set<Edge<V, E>> edges = new HashSet<>(); //用来存储边
	Comparator<Edge<V, E>> edgeComparator = (Edge<V, E> e1, Edge<V, E> e2) -> {
		return weightManager.compare(e1.weight, e2.weight);
	};
	
	public ListGraph(WeightManager<E> weightManager) {
		super(weightManager);
	}

	
	@Override
	public int verticesSize() {
		return vertices.size();
	}

	@Override
	public int edgesSize() {
		return edges.size();
	}

	@Override
	public void addVertex(V v) {
		if (vertices.containsKey(v)) return;
		vertices.put(v, new Vertex<>(v));
	}

	@Override
	public void removeVertex(V v) {
		//删除顶点 删除顶点的所有边
		Vertex<V, E> vertex = vertices.get(v);
		if (vertex == null) return;
		//先删除 edges 中 vertex 相关的边 使用迭代
		
		Iterator<Edge<V, E>> inIterator = vertex.inEdges.iterator();
		while (inIterator.hasNext()) {
			edges.remove(inIterator.next());
			inIterator.remove();
		}
		
		Iterator<Edge<V, E>> iterator = vertex.outEdges.iterator();
		while (iterator.hasNext()) {
			edges.remove(iterator.next());
			iterator.remove();
		}
	}

	@Override
	public void addEdge(V fromV, V toV) {
		addEdge(fromV, toV, null);
	}

	@Override
	public void addEdge(V fromV, V toV, E weight) {
		Vertex<V, E> from = vertices.get(fromV);
		
		//如果顶点不存在就添加
		if (from == null) {
			from = new Vertex<>(fromV);
			vertices.put(fromV, from);
		}
		
		Vertex<V, E> to = vertices.get(toV);
		
		//如果顶点不存在就添加
		if (to == null) {
			to = new Vertex<>(toV);
			vertices.put(toV, to);
		}
		
		Edge<V, E> edge = new Edge<>(from, to);
		edge.weight = weight;
		
		if(edges.contains(edge)) removeEdge(fromV, toV);
		
		from.outEdges.add(edge);
		to.inEdges.add(edge);
		edges.add(edge);
	}

	@Override
	public void removeEdge(V fromV, V toV) {
		Vertex<V, E> from = vertices.get(fromV);
		Vertex<V, E> to = vertices.get(toV);
		if (from == null || to == null) return;
		Edge<V, E> edge = new Edge<V, E>(from, to);
		if (edges.remove(edge)) {
			from.outEdges.remove(edge);
			to.inEdges.remove(edge);
		}
		
	}
	
	//顶点类
	private static class Vertex<V, E> {
		V value;
		Set<Edge<V, E>> inEdges = new HashSet<>();
		Set<Edge<V, E>> outEdges = new HashSet<>();
		
		Vertex(V value) {
			this.value = value;
		}
		
		@Override
		public boolean equals(Object obj) {
			Vertex<V, E> vertex = (Vertex<V, E>) obj;
			return Objects.equals(vertex.value, value);
		}
		
		@Override
		public int hashCode() {
			return value == null ? 0 : value.hashCode();
		}

		@Override
		public String toString() {
			return value == null ? "null" : value.toString();
		}
		
	}
	
	//边类
	private static class Edge<V, E> {
		Vertex<V, E> from;
		Vertex<V, E> to;
		E weight;
		
		public Edge(Vertex<V, E> from, Vertex<V, E> to) {
			this(from, to, null);
		}

		public Edge(Vertex<V, E> from, Vertex<V, E> to, E weight) {
			this.from = from;
			this.to = to;
			this.weight = weight;
		}
		
		public EdgeInfo<V, E> edgeInfo(){
			return new EdgeInfo<>(from.value, to.value, weight);
		}
		
		@Override
		public boolean equals(Object obj) {
			Edge<V, E> edge = (Edge<V, E>) obj;
			return Objects.equals(edge.from, from) && Objects.equals(edge.to, to);
		}
		
		@Override
		public int hashCode() {
			return from.hashCode() * 31 + to.hashCode();
		}
		
		@Override
		public String toString() {
			return "Edge [from=" + from + ", to=" + to + ", weight=" + weight + "]";
		}
		
	}
	
	
	/**
	 *  广度优先算法 拓扑排序
	 */
	public List<V> topologicalSortBfs() {
		//先搞一个list集合来存放 排好序的 元素
		List<V> list = new ArrayList<>(); 
		//再整一个队列来 存放 入度为0 的顶点 入度为0就意味着它没有指向任何顶点，不用依赖任何顶点
		Queue<Vertex<V, E>> queue = new LinkedList<>();
		//搞个map集合来存放入度不为0的顶点 key为顶点对象 value为入度数量
		Map<Vertex<V, E>, Integer> map = new HashMap<>();
		//遍历一下 全部 顶点 先把 入度 为 0 的顶点入队，把入度不为0顶点添加到map里面
		vertices.forEach((V key, Vertex<V, E> vertex)->{
			int inSize = vertex.inEdges.size();
			if (inSize == 0) {
				queue.offer(vertex);
			}else {
				map.put(vertex, inSize);
			}
		});
		
		//遍历队列
		while (!queue.isEmpty()) {
			Vertex<V,E> poll = queue.poll();
			//将出队顶点的值添加到list集合中
			list.add(poll.value);
			//改变 该顶点 出度 边指向顶点的 入度
			poll.outEdges.forEach((Edge<V, E> edge)->{
				int inSize = map.get(edge.to) - 1;
				if (inSize == 0) {
					queue.offer(edge.to);
				}else {
					map.put(edge.to, inSize);
				}
			});
		}
		
		//判断 这个图是否存在 环  拓扑排序 只能应用于 有向无环图 DAG
		if (list.size() != vertices.size()) {
			throw new RuntimeException("图中存在环或者没有方向，无法进行拓扑排序！");
		}
		return list;
	}
	
	
	/**
	 * 深度优先 拓扑排序
	 * @return
	 */
	public List<V> topologicalSortDfs() {
		//用来 存放 排序后的值
		LinkedList<V> list = new LinkedList<>();
		//用来存放已经访问过的顶点
		Set<Vertex<V, E>> set = new HashSet<>();
		//辅助栈 用来存放遍历到的顶点
		Stack<Vertex<V, E>> stack = new Stack<>();
		//遍历map中全部的顶点
		vertices.forEach((V key, Vertex<V, E> vertex)->{
			//如果这个顶点已经被访问过 就跳过 这里的 return 等价于 continue
			if (set.contains(vertex)) return;
			//将遍历到的顶点添加到栈中
			stack.push(vertex);
			//将遍历到的顶点添加到set集合中标识 代表该顶点已经被访问
			set.add(vertex);
			//遍历栈
			while(!stack.isEmpty()) {
				//弹出栈顶元素
				Vertex<V,E> pop = stack.pop();
				//判断栈顶元素的初度是否为0 为零就直接添加到目标集合的首位
				if(pop.outEdges.size() == 0) {
					list.addFirst(pop.value); 
					//跳过后面的操作
					continue;
				}
				//声明一个标识 变量
				boolean flag = false;
				//遍历 栈顶  顶点 指向顶点
				for (Edge<V, E> edge : pop.outEdges) {
					//如果该顶点已经访问了 就跳过该顶点 继续下一个顶点
					if (set.contains(edge.to)) {
						//如果执行了下面代码跳出了循环 就代表以pop为出发点的顶点出发的顶点
						//全部已经访问完 所以 标识pop顶点能添加到目标 集合里面去
						flag = true; 
						continue;
					}
					//如果能够执行这下面的代码 代表以该顶点为出发点的顶点还没有访问完
					//所以 标识栈顶 的顶点不能添加到目标 集合里面去
					flag = false; 
					//将 出发点 添加到栈中
					stack.push(pop);
					//将 终点 添加到栈中
					stack.push(edge.to);
					//将终点 添加到set集合中 标识 已经访问
					set.add(edge.to);
					break;
				}
				//将 pop 添加到目标集合的首位置
				if(flag) list.addFirst(pop.value);
			}
			
		});
		return list;
	}
	
	
	/**
	 * 广度优先遍历 利用队列
	 * @param vertex
	 */
	public void bfs(V value, VertexVisitor<V> visitor) {
		Vertex<V, E> vertex = vertices.get(value);
		if (vertex == null) return;
		Set<Vertex<V, E>> set = new HashSet<>();
		Queue<Vertex<V, E>> queue = new LinkedList<>();
		queue.offer(vertex);
		set.add(vertex);
		while (!queue.isEmpty()) {
			Vertex<V, E> poll = queue.poll();
			if (visitor.visit(poll.value)) return;
			for (Edge<V, E> edge : poll.outEdges) {
				if (!set.contains(edge.to)) {
					queue.offer(edge.to);
					set.add(edge.to);
				}
			}
		}
	}
	
	/**
	 * 深度优先遍历 利用栈 迭代
	 * @param vertex
	 */
	public void dfs(V value, VertexVisitor<V> visitor) {
		Vertex<V, E> vertex = vertices.get(value);
		if (vertex == null) return;
		Set<Vertex<V, E>> set = new HashSet<>();
		Stack<Vertex<V, E>> stack = new Stack<>();
		stack.push(vertex);
		set.add(vertex);
		if (visitor.visit(vertex.value)) return;
		while (!stack.isEmpty()) {
			Vertex<V, E> pop = stack.pop();
			for (Edge<V, E> edge : pop.outEdges) {
				if (set.contains(edge.to)) continue;
				stack.push(pop);
				stack.push(edge.to);
				set.add(edge.to);
				if (visitor.visit(edge.to.value)) return;
				break;
			}
		}
	}
	
	/**
	 * 深度优先遍历 递归
	 * @param vertex
	 */
	public void dfsRecursive(V value, VertexVisitor<V> visitor) {
		Vertex<V, E> vertex = vertices.get(value);
		if (vertex == null) return;
		dfs(vertex, new HashSet<>(), visitor);
	}
	
	private void dfs(Vertex<V, E> vertex, Set<Vertex<V, E>> visitVertices, VertexVisitor<V> visitor) {
		//直接打印 开始顶点
		if(visitor.visit(vertex.value)) return;
		//把遍历过的 顶点 添加到 visitVertices 集合中
		visitVertices.add(vertex);
		for (Edge<V, E> edge : vertex.outEdges) {
			if (visitVertices.contains(edge.to)) continue;
			dfs(edge.to, visitVertices, visitor);
		}
	}
	
	private Set<EdgeInfo<V, E>> prim(){
		//声明一个set集合用来储存 最小生成树的边
		Set<EdgeInfo<V, E>> edgesInfo = new HashSet<>();
		//再声明一个set集合用来储存 以及连通的顶点
		Set<Vertex<V, E>> vertexes = new HashSet<>();
		//选任意顶点 对其 outEdges进行第一次切分
		Iterator<Vertex<V, E>> iterator = vertices.values().iterator();
		//如果图没有顶点 直接return null
		if (!iterator.hasNext()) return null;
		//拿到 一个 顶点 发出的边  进行切分
		Vertex<V, E> vertex = iterator.next();
		vertexes.add(vertex);
		
		//搞个最小堆来 获得 被切分边中权值最小的一条边
		MinHeap<Edge<V, E>> heap = new MinHeap<>(vertex.outEdges, edgeComparator);
		while (!heap.isEmpty()) {
			Edge<V, E> remove = heap.remove();
			//如果发现这条边指向的顶点已经被访问就跳过
			if (vertexes.contains(remove.to)) continue;
			vertexes.add(remove.to);
			edgesInfo.add(remove.edgeInfo());
			//将 最小边指向 的 顶点 发出的所有边 都添加 到最小堆中   此处可以过滤已经添加过的边
			heap.addAll(remove.to.outEdges);
		}
		return edgesInfo;
	}
	
	private Set<EdgeInfo<V, E>> kruskal(){
		//声明一个set集合用来储存 最小生成树的边
		Set<EdgeInfo<V, E>> edgesInfo = new HashSet<>();
		//如果边数小于1 直接返回空
		if (verticesSize() < 1) return null;
		//批量建堆 把所有的边都添加到最小堆里面去
		MinHeap<Edge<V, E>> heap = new MinHeap<>(edges, edgeComparator);
		//用并查集来存储 遍历出来边的 起点 终点
		//如果遍历出来的边 的顶点已经 在一个集合里面就不需要添加到集合里面
		GenericUnionFind<Vertex<V, E>> uf = new GenericUnionFind<>();
		//先初始所有顶点 把顶点添加到并查集中
		vertices.forEach((V key, Vertex<V, E> vertex) -> {
			uf.makeSet(vertex);
		});
		int size = verticesSize() - 1;
		//最小生成树的边的数量 等于顶点 数量减一
		while (edgesInfo.size() < size) {
			Edge<V,E> remove = heap.remove();
			boolean same = uf.isSame(remove.from, remove.to);
			//如果 这两个顶点已经 在一个集合了 就不需要添加这条边 会构成环
			if (same) continue;
			edgesInfo.add(remove.edgeInfo());
			uf.union(remove.from, remove.to);
		}
		return edgesInfo;
	}

	@Override
	public String toString() {
		return "ListGraph [vertices=" + vertices + ", edges=" + edges + "]";
	}


	@Override
	public Set<EdgeInfo<V, E>> mst() {
		return Math.random() > 0.5 ? prim() : kruskal();
	}


	@Override
	public Map<V, PathInfo<V, E>> shortestPath(V begin) {
		return dijkstra(begin);
	}
	
	private Map<V, PathInfo<V, E>> bellmanFord(V begin){
		//用来返回 的 集合
		Map<V, PathInfo<V, E>> selectedPaths = new HashMap<>();
		//添加所有边
		int times = verticesSize() - 1;
		selectedPaths.put(begin, new PathInfo<>(weightManager.zero()));
		for (int i = 0; i < times; i++) {
			for (Edge<V, E> edge : edges) {
				if(!selectedPaths.containsKey(edge.from.value)) continue;
				relaxForbellmanFord(edge, selectedPaths);
			}
		}
		selectedPaths.remove(begin);
		return selectedPaths;
	}
	
	/**
	 * 
	 * @param edge
	 * @param selectedPaths
	 */
	private void relaxForbellmanFord(Edge<V, E> edge, Map<V, PathInfo<V, E>> selectedPaths) {
		PathInfo<V, E> pathInfo = selectedPaths.get(edge.from.value);
		if (pathInfo == null) return;
		
		E newWeight = weightManager.add(pathInfo.weight, edge.weight);
		
		PathInfo<V, E> oldPath = selectedPaths.get(edge.to.value);
		
		if(oldPath == null || weightManager.compare(newWeight, oldPath.weight) < 0) {
			if (oldPath == null) {
				oldPath = new PathInfo<>();
			}else {
				oldPath.edgeInfos.clear();
			}
			oldPath.weight = newWeight;
			oldPath.edgeInfos.addAll(pathInfo.edgeInfos);
			oldPath.edgeInfos.add(edge.edgeInfo());
			
			selectedPaths.put(edge.to.value, oldPath);
		}
	}


	@SuppressWarnings("unused")
	private Map<V, PathInfo<V, E>> dijkstra(V begin) {
		//返回 起点到 这个点 的最小路径权值和
		Map<V, PathInfo<V, E>> selectedPaths = new HashMap<>();
		//用一个map集合来存取 要松弛的边
		Map<Vertex<V, E>, PathInfo<V, E>> paths = new HashMap<>();
		//获得起点
		Vertex<V, E> vertex = vertices.get(begin);
		//遍历顶点的 出度边
		paths.put(vertex, new PathInfo<>(weightManager.zero()));
		
		while(!paths.isEmpty()) {
			//从 paths 里面取出 权值最小的 边 指向的顶点
			Entry<Vertex<V, E>, PathInfo<V, E>> minEntry = getMinPath(paths);
			Vertex<V, E> minVertex = minEntry.getKey();
			PathInfo<V, E> minPath = minEntry.getValue();
			//将权值最小的边添加到map中并从outEdges移除
			selectedPaths.put(minVertex.value, minPath);
			paths.remove(minVertex);
			//遍历 下一个 可能 minEdges 被拉起的 顶点
			for(Edge<V, E> edge : minVertex.outEdges) {
				//如果 这个顶点已经 被 拉起来了就不需要 松弛
				if (selectedPaths.containsKey(edge.to.value)) continue;
				relaxForDijkstra(edge, minPath, paths);
			}
		}
		selectedPaths.remove(begin);
		return selectedPaths;
	}
	
	private void relaxForDijkstra(Edge<V, E> edge, PathInfo<V, E> minPath, Map<Vertex<V, E>, PathInfo<V, E>> paths) {
		PathInfo<V, E> oldPathInfo = paths.get(edge.to);
		E newWeight = weightManager.add(minPath.weight, edge.weight);
		if (oldPathInfo == null || weightManager.compare(newWeight, oldPathInfo.weight) < 0) {
			if (oldPathInfo == null) {
				oldPathInfo = new PathInfo<>();
			}else {
				oldPathInfo.edgeInfos.clear();
			}
			oldPathInfo.weight = newWeight;
			oldPathInfo.edgeInfos.addAll(minPath.getEdgeInfos());
			oldPathInfo.edgeInfos.add(edge.edgeInfo());
			paths.put(edge.to, oldPathInfo);
		}
	}

//	private Map<V, E> dijkstra1(V begin) {
//		//返回 起点到 这个点 的最小路径权值和
//		Map<V, E> map = new HashMap<>();
//		//用一个map集合来存取 要松弛的边
//		Map<Vertex<V, E>, E> paths = new HashMap<>();
//		//获得起点
//		Vertex<V, E> vertex = vertices.get(begin);
//		
//		//遍历顶点的 出度边
//		for(Edge<V, E> edge : vertex.outEdges) {
//			paths.put(edge.to, edge.weight);
//		}
//		
//		while(!paths.isEmpty()) {
//			//从 map 里面取出 权值最小的 边 指向的顶点
//			Vertex<V, E> minVertex = minEdges(paths);
//			//将权值最小的边添加到map中并从outEdges移除
//			map.put(minVertex.value, paths.get(minVertex));
//			paths.remove(minVertex);
//			//遍历 下一个 可能 minEdges 被拉起的 顶点
//			for(Edge<V, E> edge : minVertex.outEdges) {
//				//如果 这个顶点已经 被 拉起来了就不需要 松弛
//				if (map.get(edge.to.value) != null) continue;
//				E oldWeight = paths.get(edge.to);
//				E newWeight = weightManager.add(map.get(minVertex.value), edge.weight);
//				if (oldWeight == null || weightManager.compare(newWeight, oldWeight) < 0) {
//					paths.put(edge.to, newWeight);
//				}
//			}
//		}
//		map.remove(begin);
//		return map;
//	}
//	
//	private Vertex<V, E> minEdges(Map<Vertex<V, E>, E> outEdges){
//		E min = null;
//		Vertex<V, E> vertex = null;
//		for (Entry<Vertex<V, E>, E> entry : outEdges.entrySet()) {
//			E value = entry.getValue();
//			if (min == null || weightManager.compare(value, min) < 0) {
//				min = value;
//				vertex = entry.getKey();
//			}
//		}
//		return vertex;
//	}
	
	private Entry<Vertex<V, E>, PathInfo<V, E>> getMinPath(Map<Vertex<V, E>, PathInfo<V, E>> paths){
		Iterator<Entry<Vertex<V, E>, PathInfo<V, E>>> iterator = paths.entrySet().iterator();
		Entry<Vertex<V, E>, PathInfo<V, E>> minPath = iterator.next();
		while(iterator.hasNext()) {
			Entry<Vertex<V, E>, PathInfo<V, E>> next = iterator.next();
			if (weightManager.compare(next.getValue().weight, minPath.getValue().weight) < 0) {
				minPath = next;
			}
		}
		return minPath;
	}


	@Override
	public Map<V, Map<V, PathInfo<V, E>>> shortestPath() {
		Map<V, Map<V, PathInfo<V, E>>> paths = new HashMap<>();
		//遍历所有边 将 所有边添加到 paths 中
		edges.forEach((Edge<V, E> edge) -> {
			//先从 paths中 获取 edge的起点 edge.from
			Map<V, PathInfo<V, E>> toMap = paths.get(edge.from.value);
			//如果 toMap 为空 就代表 edge.from 还不存在paths中 
			if (toMap == null) {
				toMap = new HashMap<>();
				//将 edge.from 作为起点的顶点添加到paths 中
				paths.put(edge.from.value, toMap);
			}
			//创建 一条路径信息 并且设置其weight
			PathInfo<V, E> pathInfo =  new PathInfo<V, E>(edge.weight);
			//将当前遍历的边 添加到 路径集中 实际每个pathInfo.edgeInfos 中只含有一条边
			pathInfo.edgeInfos.add(edge.edgeInfo());
			//将终点 添加到toMap中 toMap的大小可能有多个
			toMap.put(edge.to.value, pathInfo);
		});
		
		vertices.forEach((V k, Vertex<V, E> vertexk) -> {
			vertices.forEach((V i, Vertex<V, E> vertexi) -> {
				vertices.forEach((V j, Vertex<V, E> vertexj) -> {
					//k i j 三者需要满足互不相等
					if (k.equals(i) || k.equals(j) || i.equals(j)) return;
					//获取 i -> k 的路径
					PathInfo<V,E> pathIK = getPathInfo(i, k, paths);
					if (pathIK == null) return;
					//获取 k -> j 的路径
					PathInfo<V,E> pathKJ = getPathInfo(k, j, paths);
					if (pathKJ == null) return;
					//获取 i -> j 的路径
					PathInfo<V,E> pathIJ = getPathInfo(i, j, paths);
					//计算出新路径 的权值
					E newWeight = weightManager.add(pathIK.weight, pathKJ.weight);
					//如果 旧路径 不等于 空 且权值 小于等于 新权值 则不需要 更新
					if (pathIJ != null && weightManager.compare(pathIJ.weight, newWeight) <= 0) return;
					//如果旧路径等于空 
					if (pathIJ == null) {
						//创建新的路径
						pathIJ = new PathInfo<>(newWeight);
						paths.get(i).put(j, pathIJ);
					}else {
						pathIJ.edgeInfos.clear();
					}
					pathIJ.weight = newWeight;
					pathIJ.edgeInfos.addAll(pathIK.edgeInfos);
					pathIJ.edgeInfos.addAll(pathKJ.edgeInfos);
					
				});
			});
		});
		return paths;
	}


	private PathInfo<V, E> getPathInfo(V v1, V v2, Map<V, Map<V, PathInfo<V, E>>> paths) {
		Map<V, PathInfo<V, E>> map = paths.get(v1);
		if (map == null || map.get(v2) == null) return null;
		return map.get(v2);
	}
	
}
