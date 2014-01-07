package com.aschenoni.PoroNet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class GameConstants {
	
	//MATCH MAKING QUEUES
	private static final int NORMALBLIND5v5 = 2;
	private static final int RANKEDSOLO5v5 = 4;
	private static final int COOPVAI5v5 = 7;
	private static final int NORMALBLIND3v3 = 8;
	private static final int NORMALDRAFT5v5 = 14;
	private static final int DOMINIONBLIND5v5 = 16;
	private static final int DOMINIONDRAFT5v5 = 17;
	private static final int COOPvAIDOMINION = 25;
	private static final int RANKEDTEAM3v3 = 41;
	private static final int RANKEDTEAM5v5 = 42;
	private static final int COOPvAI3v3 = 52;
	private static final int ARAM = 65;
	private static final int COOPvAIARAM = 67;
	private static final int ONEFORALL5v5 = 70;
	private static final int SHOWDOWN1v1 = 72;
	private static final int SHOWDOWN2v2 = 73;
	
	//KEYS
	private List<Node> nodes = new ArrayList<Node>();
	
	//DB CONNECTOR
	MongoClient client;
	
	public GameConstants(MongoClient client) throws MalformedURLException, IOException
	{
		this.client = client;
		setupKeys();
		//setupChamps();
		
	}
	
	public Node getHead()
	{
		return nodes.get(0);
	}
	
	private void setupKeys()
	{
		//setup Circularly linked list
		nodes.add(new Node("321fa93f-d4fc-4eef-bfb2-08454396fd79"));
		nodes.add(new Node(nodes.get(0), "df2e07e0-3c63-41c2-b634-2cda21c7759f"));
		nodes.add(new Node(nodes.get(1), "6622a609-4c80-4c2f-8ff5-a0fb6c313f58"));
		nodes.add(new Node(nodes.get(2), "1d4cd611-fc4c-4e17-9bdb-1e0eab0d8536"));
		nodes.add(new Node(nodes.get(3), "0f4839e9-0ad5-4ac9-89e4-4fcec8919fc4"));
		nodes.add(new Node(nodes.get(4), "5fea8d51-7b65-4864-983a-daf2fb0af182"));
		nodes.add(new Node(nodes.get(5), "64251506-c1f1-4dfc-84a2-fbac1c2bf70c"));
		nodes.add(new Node(nodes.get(6), "51e8348a-bdc3-4803-a02e-fd9439120a53"));
		nodes.add(new Node(nodes.get(7), "55ef967c-6556-4531-9b29-de8005a4c679"));
		nodes.add(new Node(nodes.get(8), "e02397dd-e134-4eed-8d70-1d73eb9415d4"));
		nodes.add(new Node(nodes.get(9), "7f6b6455-e865-4965-b1dc-84faa5fcacef"));
		nodes.add(new Node(nodes.get(10),"e329d45c-bb56-464a-8897-03daf5c2d369"));
		
		nodes.get(0).setPrevNext(nodes.get(11), nodes.get(1));
		nodes.get(11).setNext(nodes.get(0));
		int i = 1;
		while(i < 11)
		{
			nodes.get(i).setNext(nodes.get(i+1) );
			i++;
		}
	}
	
	@SuppressWarnings("unused")
	private void setupChamps() throws MalformedURLException, IOException
	{
		DB riotData = client.getDB("RiotData");
		DBCollection champColl = riotData.getCollection("champions");
		//document needs: champId, champName, gold per min average, kills per min average, deaths per min average, assists per min average
		String champions = URLmaker.champion("na", false, nodes.get(0).value() );
		champions = IOUtils.toString(new URL(champions));
		
		BasicDBObject championList = (BasicDBObject) JSON.parse(champions);
		BasicDBList actualList = (BasicDBList) championList.get("champions");
		
		for(Object champ1: actualList)
		{
			DBObject champ = (DBObject) champ1;
			DBObject trunkChamp = new BasicDBObject();
			trunkChamp.put("id", champ.get("id"));
			if(champColl.findOne(trunkChamp) == null)
			{
				trunkChamp.put("name", champ.get("name"));
				champColl.insert(trunkChamp);
				System.out.println(champ.get("name") + " was inserted into the DB");
			}
			
			
			
		}	
	}
}
