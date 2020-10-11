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
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

@SuppressWarnings("all")
public class ListGraph2<V, E> extends Graph<V, E> {

	Map<V, Vertex<V, E>> vertices = new HashMap<>(); //用来存储 顶点
	Set<Edge<V, E>> edges = new HashSet<>(); //用来存储边
	Comparator<Edge<V, E>> edgeComparator = (Edge<V, E> e1, Edge<V, E> e2) -> {
		return weightManager.compare(e1.weight, e2.weight);
	};
	
	public ListGraph2(WeightManager<E> weightManager) {
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
		return bellmanFord(begin);
	}
	
	private Map<V, PathInfo<V, E>> bellmanFord(V begin){
		//获取 源点
		Vertex<V, E> vertex = vertices.get(begin);
		//如果 传入 的源点 不存在 就 返回 空  或者  源点 的 outEdges.size() 小于 1 就返回空 
		if (vertex == null || vertex.outEdges.size() < 1) return null;
		
		//声明一个Map 集合 用来存储 源点到各个顶点的路径以及权值信息 并 返回给外界
		Map<V, PathInfo<V, E>> selectedPaths = new HashMap<>();
		//先将 源点 添加到 selectedPaths 集合中 并设置其路径权值为 zero 代表以源点 为起点直接发出的边都是可以松弛的
		selectedPaths.put(begin, new PathInfo<>(weightManager.zero()));
		//对 所有边 进行 V - 1 次松弛 操作 V代表顶点数量
		int times = verticesSize() - 1;
		for (int i = 0; i < times; i++) {
			//遍历 所有的边 
			edges.forEach((Edge<V, E> edge) -> {
				//如果 edge 的起点都没有 松弛 那么 由edge.from 指向 edge.to的这条边肯定松弛不了 就跳过
				if (!selectedPaths.containsKey(edge.from.value)) return; //continue;
				//松弛
				relaxForbellmanFord(edge, selectedPaths);
			});
		}
		//最后 记得 把源点 从 selectedPaths中移除
		selectedPaths.remove(begin);
		return selectedPaths;
	}
	
	/**
	 * 
	 * @param edge 被松弛的边
	 * @param selectedPaths //已经松弛了的边
	 */
	private void relaxForbellmanFord(Edge<V, E> edge, Map<V, PathInfo<V, E>> selectedPaths) {
		//先 从selectedPaths 获取 edge.to 的路径信息
		PathInfo<V, E> oldPath = selectedPaths.get(edge.to.value);
		//先获得起点 的 路径信息
		PathInfo<V, E> fromPath = selectedPaths.get(edge.from.value);
		//获取新路径 的 权值
		E newWeight = weightManager.add(fromPath.weight, edge.weight);
		//如果 就路径 不等于空 且 权值小于等于 新路径的权值 就不需要松弛
		if (oldPath != null && weightManager.compare(oldPath.weight, newWeight) <= 0) return;
		if (oldPath == null) {
			//创建 一个 没有任何信息 的新路径 并赋值给oldPath
			oldPath = new PathInfo<>();
			//先把新创建的路径加入到 paths 中 后面 再设置 路径的信息
			selectedPaths.put(edge.to.value, oldPath);
		}else {
			//如果 旧路径不等于空 并且 新路径的权值 小于 旧路径 用新路径 覆盖 selectedPaths 中的旧路径 
			//先清空 旧路径 信息
			oldPath.edgeInfos.clear();
		}
		//设置权值
		oldPath.weight = newWeight;
		// 添加 edge.from 起点的路径信息
		oldPath.edgeInfos.addAll(fromPath.edgeInfos);
		//再 添加 edge 这条边
		oldPath.edgeInfos.add(edge.edgeInfo());
	}


	private Map<V, PathInfo<V, E>> dijkstra(V begin) {
		//获取 源点
		Vertex<V, E> vertex = vertices.get(begin);
		//如果 传入 的源点 不存在 就 返回 空 或者  源点 的 outEdges.size() 小于 1 就返回空 
		if (vertex == null || vertex.outEdges.size() < 1) return null;
		//声明一个Map 集合 用来存储 源点到各个顶点的路径以及权值信息 并 返回给外界
		Map<V, PathInfo<V, E>> selectedPaths = new HashMap<>();
		//声明一个临时的Map 用来 保存 可能 被 “拉起” 的顶点 但是还未被 “拉起” 
		//key为终点 value为源点到终点的路径以及权值信息
		Map<Vertex<V, E>, PathInfo<V, E>> paths = new HashMap<>();
		//将 源点 添加 到 paths 集合 中 并设置其 路径总权值为 zero 添加的目的是为了松弛 源点的 outEdges
		paths.put(vertex, new PathInfo<>(weightManager.zero()));
		//遍历paths 直到paths 为空
		while (!paths.isEmpty()) {
			//找出paths中 即将被 “拉起” 的 一个键值对(顶点) 也就是 总权值最小的那个顶点 这个操作是循环的
			Entry<Vertex<V, E>, PathInfo<V, E>> minEntry = getMinPath(paths);
			//代表 即将 被 “拉起” 的顶点
			Vertex<V,E> flyVertex = minEntry.getKey();
			//路径信息 可以通过minEntry.getValue()获得
			PathInfo<V, E> minPath = minEntry.getValue();
			//将其 添加 到 selectedPaths 中 并从 paths 中移除 代表 flyVertex 以及被拉起 考虑flyVertex为源点
			selectedPaths.put(flyVertex.value, minPath);
			//将flyVertex 从 paths 中移除 
			paths.remove(flyVertex);
			//对 “被拉起” 的顶点 即flyVertex 的 outEdges进行 松弛
			for (Edge<V, E> edge : flyVertex.outEdges) {
				//针对无向图 如果该条边的终点指向的顶点已经被“拉起” 就不需要对这条边进行松弛 
				//selectedPaths 包含 edge.to.value 及代表 edge.to 顶点 已经被拉起
				if (selectedPaths.containsKey(edge.to.value)) continue;
				//然后对 需要松弛的 边进行 松弛 操作
				relaxForDijkstra(edge, minPath, paths);
			}
		}
		//最后 记得 把源点 从 selectedPaths中移除
		selectedPaths.remove(begin);
		return selectedPaths;
	}
	
	/**
	 * 
	 * @param edge 对 edge 边 进行 松弛  
	 * @param minPath 代表从源点到 edge.from 的路径信息
	 * @param paths 可能被拉起的顶点 路径集合
	 * 
	 * 松弛：代表着edge边的起点from顶点 已经被 “拉起” 
	 * 由edge.from指向的顶点在下一次“拉升” 也可能会被拉起 所以需要将这些 顶点 添加到 paths中
	 */
	private void relaxForDijkstra(Edge<V, E> edge, PathInfo<V, E> minPath, Map<Vertex<V, E>, PathInfo<V, E>> paths) {
		//先从paths中获取edge边指向的顶点edge.to 判断从源点到edge.to这条边以及被松弛过
		PathInfo<V, E> oldPath = paths.get(edge.to);
		//如果 oldPath 不等空 意味着 从源点 到edge.to有其他路径 并且已经被 添加到 了paths 即已经被松弛了
		//只需对比 新路径 的 总权值 和 旧路径的 总权值 如果新路径的总权值比旧路径的总权值小就 用新的覆盖旧的
		//计算新路径的总权值
		E newWeight = weightManager.add(minPath.weight, edge.weight);
		//如果 旧路径不等于空 并且 新路径的权值 大于 旧路径 就不需要 松弛
		if (oldPath != null && weightManager.compare(newWeight, oldPath.weight) >= 0) return;
		//oldPath 为空 意味着 从源点到edge.to这条边第一次被松弛
		if (oldPath == null) {
			//创建 一个 没有任何信息 的新路径 并赋值给oldPath
			oldPath = new PathInfo<>();
			//先把新创建的路径加入到 paths 中 后面 再设置 路径的信息
			paths.put(edge.to, oldPath);
		}else {
			//如果 旧路径不等于空 并且 新路径的权值 小于 旧路径 用新路径 覆盖 paths 中的旧路径 
			//先清空 旧路径 信息
			oldPath.getEdgeInfos().clear();
		}
		//设置权值
		oldPath.weight = newWeight;
		//先将 “被拉起” 顶点 的路径 添加 到路径信息中 因为 edge.to 是因为 edge.from顶点被拉起才可能被拉起的
		oldPath.getEdgeInfos().addAll(minPath.edgeInfos);
		//再 添加 edge 这条边
		oldPath.getEdgeInfos().add(edge.edgeInfo());
	}

	/**
	 * 从 paths 中获得 权值最小的边信息 
	 * @param paths
	 * @return
	 */
	private Entry<Vertex<V, E>, PathInfo<V, E>> getMinPath(Map<Vertex<V, E>, PathInfo<V, E>> paths){
		Iterator<Entry<Vertex<V, E>, PathInfo<V, E>>> iterator = paths.entrySet().iterator();
		Entry<Vertex<V, E>, PathInfo<V, E>> minEntry = iterator.next();
		while (iterator.hasNext()) {
			Entry<Vertex<V, E>, PathInfo<V, E>> entry = iterator.next();
			if (weightManager.compare(entry.getValue().weight, minEntry.getValue().weight) < 0) {
				minEntry = entry;
			}
		}
		return minEntry;
	}


	@Override
	public Map<V, Map<V, PathInfo<V, E>>> shortestPath() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
