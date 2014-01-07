package com.aschenoni.PoroNet;

import java.util.concurrent.TimeUnit;

public class Node
	{
		private Node next;
		private Node prev;
		public int actions1;
		public int actionsSec;
		String val;
		public  int currentSec;
		public int currentMin;
		
		public Node(String val)
		{
			this.val = val;
			actions1 = 0;
			actionsSec = 0;
			currentSec = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() );
			currentMin = (int) (System.currentTimeMillis() /1000 / 60)% 100;
		}
		
		public Node(Node prev, Node next, String val)
		{
			this(val);
			this.next = next;
			this.prev = prev;
		}
		
		public Node(Node prev, String val)
		{
			this(val);
			this.prev = prev;
		}
		
		Node next()
		{
			return this.next;
		}
		
		Node prev()
		{
			return this.prev;
		}
		
		void setNext(Node node)
		{
			next = node;
		}
		
		void setPrev(Node node)
		{
			prev = node;
		}
		
		void setPrevNext(Node prev, Node next)
		{
			this.prev = prev;
			this.next = next;
		}

		String value()
		{
			return val;
		}
	}
