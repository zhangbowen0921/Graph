package com.zbowen.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zbowen.graph.Graph.EdgeInfo;
import com.zbowen.graph.Graph.PathInfo;
import com.zbowen.graph.Graph.WeightManager;

public class Main {

	static WeightManager<Double> weightManager = new WeightManager<Double>() {
		public int compare(Double w1, Double w2) {
			return w1.compareTo(w2);
		}

		public Double add(Double w1, Double w2) {
			return w1 + w2;
		}

		@Override
		public Double zero() {
			return 0.0;
		}
	};
	
	/**
	 * 有向图
	 */
	private static Graph<Object, Double> directedGraph(Object[][] data) {
		Graph<Object, Double> graph = new ListGraph<>(weightManager);
		for (Object[] edge : data) {
			if (edge.length == 1) {
				graph.addVertex(edge[0]);
			} else if (edge.length == 2) {
				graph.addEdge(edge[0], edge[1]);
			} else if (edge.length == 3) {
				double weight = Double.parseDouble(edge[2].toString());
				graph.addEdge(edge[0], edge[1], weight);
			}
		}
		return graph;
	}
	
	/**
	 * 无向图
	 * @param data
	 * @return
	 */
	private static Graph<Object, Double> undirectedGraph(Object[][] data) {
		Graph<Object, Double> graph = new ListGraph<>(weightManager);
		for (Object[] edge : data) {
			if (edge.length == 1) {
				graph.addVertex(edge[0]);
			} else if (edge.length == 2) {
				graph.addEdge(edge[0], edge[1]);
				graph.addEdge(edge[1], edge[0]);
			} else if (edge.length == 3) {
				double weight = Double.parseDouble(edge[2].toString());
				graph.addEdge(edge[0], edge[1], weight);
				graph.addEdge(edge[1], edge[0], weight);
			}
		}
		return graph;
	}
	
	public static void testPrimAndKruskal() {
		Graph<Object, Double> graph = undirectedGraph(Data.MST_01);
		Set<EdgeInfo<Object,Double>> mst = graph.mst();
		for (EdgeInfo<Object, Double> edgeInfo : mst) {
			System.out.println(edgeInfo);
		}
	}

	public static void testTopologicalSort() {
		Graph<Object, Double> graph = directedGraph(Data.TOPO);
		List<Object> list = graph.topologicalSortBfs();
		for (Object object : list) {
			System.out.println(object);
		}
	}
	
	public static void main(String[] args) {
		//testTopologicalSort();
		//testPrimAndKruskal();
		Graph<Object, Double> graph = directedGraph(Data.WEIGHT3);
		Map<Object, Map<Object, PathInfo<Object, Double>>> map = graph.shortestPath();
		map.forEach((Object from, Map<Object, PathInfo<Object, Double>> toMap) -> {
			System.out.println("from：" + from + "------------------------");
			toMap.forEach((Object to, PathInfo<Object, Double> path) -> {
				System.out.println("to：" + to + "\tWeight：" + path.weight + "\t" + path.edgeInfos);
			});
		});
	}
}
