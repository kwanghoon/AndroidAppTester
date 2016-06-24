package kr_ac_yonsei_mobilesw_Grouping;

import java.util.Vector;

public class List {

	// 리스트 변수
	private Node header;
	private int listLength;

	// 리스트 생성자
	public List(){
		header = new Node(-1,-1);
		listLength = 0;
	}

	// 노드
	private class Node{
			
		// 노드 변수
		private int startRow;
		private int rowLength;
			
		private int ownLength;
		private Vector<Integer> otherLength;
		private Vector<String> lcs;
		private Vector<Integer> lcsLength;
		private Vector<Double> bigSimilarity;
		private Vector<Double> smallSimilarity;
			
		private Node nextNode;
			
		// 노드 생성자
		public Node(int sr, int rl){
			startRow = sr;
			rowLength = rl;
				
			ownLength = 0;
			otherLength = new Vector<Integer>();
			lcs = new Vector<String>();
			lcsLength = new Vector<Integer>();
			bigSimilarity = new Vector<Double>();
			smallSimilarity = new Vector<Double>();
				
			nextNode = null;
		}
	}



	// 리스트 탐색 : 리스트 길이
	public int getListLength(){
		return listLength;
	}

	// 리스트 탐색 : 노드
	public Node getNode(int index){
		Node node = header.nextNode;
			
		for(int i=0; i<index; i++)
			node = node.nextNode;
			
		return node;
	}

	// 리스트 삽입 : 노드
	public void add(int sr, int rl){
		if(listLength==0){
			Node newNode = new Node(sr,rl);
				
			newNode.nextNode = header.nextNode;
			header.nextNode = newNode;
			listLength++;
		}		
		else{
			Node previous = getNode(listLength-1);
			Node next = previous.nextNode;	
			Node newNode = new Node(sr,rl);
				
			previous.nextNode = newNode;
			newNode.nextNode = next;
			listLength++;
		}
	}



	// 노드 탐색 : 시작행
	public int getStartRow(int index){
		return getNode(index).startRow;
	}

	// 노드 탐색 : 행길이
	public int getRowLength(int index){
		return getNode(index).rowLength;
	}

	// 노드 탐색 : 자신의 길이
	public int getOwnLength(int index){
		if(index==0)
			return getNode(1).otherLength.get(0);
		return getNode(index).ownLength;
	}

	// 노드 탐색 : 비교대상들의 길이 벡터
	public Vector<Integer> getOtherLength(int index){
		return getNode(index).otherLength;
	}

	// 노드 탐색 : LCS 벡터
	public Vector<String> getLcs(int index){
		return getNode(index).lcs;
	}

	// 노드 탐색 : LCS 길이 벡터
	public Vector<Integer> getLcsLength(int index){
		return getNode(index).lcsLength;
	}

	// 노드 탐색 : 큰 유사도 벡터
	public Vector<Double> getBigSimilarity(int index){
		return getNode(index).bigSimilarity;		
	}

	// 노드 탐색 : 작은 유사도 벡터
	public Vector<Double> getSmallSimilarity(int index){
		return getNode(index).smallSimilarity;		
	}

	// 노드 삽입 : 데이터 삽입
	public void saveData(int index, int ownLength, int otherLength, String lcs, int lcsLength, double bigSimilarity, double smallSimilarity){
		getNode(index).ownLength = ownLength;
		getNode(index).otherLength.addElement(otherLength);
		getNode(index).lcs.addElement(lcs);
		getNode(index).lcsLength.addElement(lcsLength);
		getNode(index).bigSimilarity.addElement(bigSimilarity);
		getNode(index).smallSimilarity.addElement(smallSimilarity);
	}

}
