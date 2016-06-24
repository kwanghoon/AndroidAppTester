package kr_ac_yonsei_mobilesw_Grouping;

import java.util.Vector;

public class List {

	// ����Ʈ ����
	private Node header;
	private int listLength;

	// ����Ʈ ������
	public List(){
		header = new Node(-1,-1);
		listLength = 0;
	}

	// ���
	private class Node{
			
		// ��� ����
		private int startRow;
		private int rowLength;
			
		private int ownLength;
		private Vector<Integer> otherLength;
		private Vector<String> lcs;
		private Vector<Integer> lcsLength;
		private Vector<Double> bigSimilarity;
		private Vector<Double> smallSimilarity;
			
		private Node nextNode;
			
		// ��� ������
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



	// ����Ʈ Ž�� : ����Ʈ ����
	public int getListLength(){
		return listLength;
	}

	// ����Ʈ Ž�� : ���
	public Node getNode(int index){
		Node node = header.nextNode;
			
		for(int i=0; i<index; i++)
			node = node.nextNode;
			
		return node;
	}

	// ����Ʈ ���� : ���
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



	// ��� Ž�� : ������
	public int getStartRow(int index){
		return getNode(index).startRow;
	}

	// ��� Ž�� : �����
	public int getRowLength(int index){
		return getNode(index).rowLength;
	}

	// ��� Ž�� : �ڽ��� ����
	public int getOwnLength(int index){
		if(index==0)
			return getNode(1).otherLength.get(0);
		return getNode(index).ownLength;
	}

	// ��� Ž�� : �񱳴����� ���� ����
	public Vector<Integer> getOtherLength(int index){
		return getNode(index).otherLength;
	}

	// ��� Ž�� : LCS ����
	public Vector<String> getLcs(int index){
		return getNode(index).lcs;
	}

	// ��� Ž�� : LCS ���� ����
	public Vector<Integer> getLcsLength(int index){
		return getNode(index).lcsLength;
	}

	// ��� Ž�� : ū ���絵 ����
	public Vector<Double> getBigSimilarity(int index){
		return getNode(index).bigSimilarity;		
	}

	// ��� Ž�� : ���� ���絵 ����
	public Vector<Double> getSmallSimilarity(int index){
		return getNode(index).smallSimilarity;		
	}

	// ��� ���� : ������ ����
	public void saveData(int index, int ownLength, int otherLength, String lcs, int lcsLength, double bigSimilarity, double smallSimilarity){
		getNode(index).ownLength = ownLength;
		getNode(index).otherLength.addElement(otherLength);
		getNode(index).lcs.addElement(lcs);
		getNode(index).lcsLength.addElement(lcsLength);
		getNode(index).bigSimilarity.addElement(bigSimilarity);
		getNode(index).smallSimilarity.addElement(smallSimilarity);
	}

}
