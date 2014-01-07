package com.aschenoni.PoroNet;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class CasesPreProcessor {
	
	static DB riotData;
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws MalformedURLException, IOException {
		
		try {
			MongoClient client = new MongoClient("localhost", 27017);
			GameConstants constants = new GameConstants(client);
			Node currentNode = constants.getHead();
			riotData = client.getDB("RiotData");
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		
		DBCollection champs = riotData.getCollection("champions");
		List<Integer> champIds = new ArrayList<Integer>();
		DBCursor champFind = champs.find();
		for(DBObject next: champFind)
		{
			champIds.add((Integer) next.get("id"));
		}
		//updateChampAverages(champIds);
		
		generate1T1E(300, champIds);
		
	}

	public static void generate1T1E(int examples, List<Integer> champs) throws IOException
	{
		FileWriter writer1 = new FileWriter("c:\\Users\\abjoy_000\\Desktop\\1T1ETrain.csv");
		FileWriter writer2 = new FileWriter("c:\\Users\\abjoy_000\\Desktop\\1T1ETest.csv");
		writer1.append("1st champ, 2nd champ, rating");
		writer1.append("\n");
		writer2.append("1st champ, 2nd champ, rating");
		writer2.append("\n");
		
		FileWriter currentWriter = writer1;
		boolean chooseWriter = true;
		
		//should return a csv file with 
		//Input1: your choice
		//Input2: opponent choice
		//output: your answer
		//output is equal to (1 + (0.2 * (WorL)) ) * ((gpm / champ average) + (kpm / champ average) - (dpm / champ average) + (apm / champ average))
		DBCollection championData = riotData.getCollection("champions"); 
		
		for(Integer champ: champs)
		{
			DBCursor cursor1 = findByEmbedObj("championId", champ);
			double champGold, champKills, champDeath, champAssists;
			
			DBObject query4 = new BasicDBObject();
			query4.put("id", champ);
			DBObject championAverages = championData.findOne(query4);
			champGold = (double) championAverages.get("goldPmin");
			champKills = (double) championAverages.get("killsPmin");
			champDeath = (double) championAverages.get("deathsPmin");
			champAssists = (double) championAverages.get("assistsPmin");
			
			int i = 0;
			while(i < examples && cursor1.hasNext())
			{
				DBObject stats = cursor1.next();
				stats = (DBObject) stats.get("gameStats");
				int team = (int) stats.get("teamId");
				BasicDBList fellowPlayers = (BasicDBList) stats.get("fellowPlayers");
				List<Integer> fellowChamps = new ArrayList<Integer>();
				for(Object fellow1: fellowPlayers)
				{
					DBObject fellow = (DBObject) fellow1;
					if((Integer) fellow.get("teamId") != team)
					{
						fellowChamps.add((Integer) fellow.get("championId") );
					}	
				}
				
				BasicDBList stats1 = (BasicDBList) stats.get("statistics");
				
				int gold = 0;
				long time = 0;
				int deaths = 0;
				int kills = 0;
				int assists = 0;
				boolean win = false;
				float goldPmin = 0;
				float deathsPmin = 0;
				float killsPmin = 0;
				float assistsPmin = 0;
				for(Object individualStat: stats1)
				{
					DBObject stat = (DBObject) individualStat;
					switch ( (Integer) stat.get("id")) {
					case 2: //gold
						gold = (int) stat.get("value");
						break;
					case 4: //deaths
						deaths = (int) stat.get("value");
						break;
					case 8: //kills
						kills = (int) stat.get("value");
						break;
					case 23://WIN
						win = true;
						break;
					case 25://LOSE
						win = false;
						break;
					case 40://time
						time = (int) stat.get("value");
						break;
					case 48://assists.
						assists = (int) stat.get("value");
						break;
					default://unused id
						break;
					}
					
					
				}
				time = TimeUnit.SECONDS.toMinutes(time);
				goldPmin = (float) gold/time;
				killsPmin = (float) kills/time;
				deathsPmin = (float) deaths/time;
				assistsPmin = (float) assists/time;
				
				goldPmin /= champGold;
				killsPmin /= champKills;
				deathsPmin /= champDeath;
				assistsPmin /= champAssists;
				
				//output is equal to (1 + (0.2 * (WorL)) ) * ((gpm / champ average) + (kpm / champ average) - (dpm / champ average) + (apm / champ average))
				double victory = 1;
				if(win)
				{
					victory = 1.2;
				}
				double rating =0;
				
				rating += (goldPmin + killsPmin + assistsPmin - deathsPmin);
				rating *= victory;
				
				if (chooseWriter)
				{currentWriter = writer1;}
				else{currentWriter = writer2;}
				
				chooseWriter = !chooseWriter;
				
				for(Integer cha: fellowChamps)
				{
					currentWriter.append(champ +"," + cha + "," + rating);
					currentWriter.append("\n");
					System.out.println(champ+ "," + cha +","+ rating);
				}
				
				
				i++;
			}
		}
		
		writer1.flush();
		writer2.flush();
		writer1.close();
		writer2.close();
		
		
	}
	

	
	public static void updateChampAverages(List<Integer> champs)
	{		
		//findByEmbedObj("championId",  1).next()
		for(Integer champ: champs)
		{
			DBCursor find = findByEmbedObj("championId", champ);
			float totalGoldpM = 0;
			float totalKillspM = 0;
			float totalDeathspM = 0;
			float totalAssistspM = 0;
			float winRate = 0;
			int exampleSize = 0;
			
			for(Object game1: find)
			{
				DBObject game = (DBObject) game1;
				DBObject gameStats = (DBObject) game.get("gameStats");
				BasicDBList stats = (BasicDBList) gameStats.get("statistics");

				int gold = -1;
				long time = -1;
				int deaths = -1;
				int kills = -1;
				int assists = -1;
				boolean win = false;
				float goldPmin = 0;
				float deathsPmin = 0;
				float killsPmin = 0;
				float assistsPmin = 0;
				for(Object individualStat: stats)
				{
					DBObject stat = (DBObject) individualStat;
					switch ( (Integer) stat.get("id")) {
					case 2: //gold
						gold = (int) stat.get("value");
						break;
					case 4: //deaths
						deaths = (int) stat.get("value");
						break;
					case 8: //kills
						kills = (int) stat.get("value");
						break;
					case 23://WIN
						win = true;
						break;
					case 25://LOSE
						win = false;
						break;
					case 40://time
						time = (int) stat.get("value");
						break;
					case 48://assists.
						assists = (int) stat.get("value");
						break;
					default://unused id
						break;
					}
					
					
				}
				time = TimeUnit.SECONDS.toMinutes(time);
				goldPmin = (float) gold/time;
				killsPmin = (float) kills/time;
				deathsPmin = (float) deaths/time;
				assistsPmin = (float) assists/time;

				totalGoldpM += goldPmin;
				totalKillspM += killsPmin;
				totalDeathspM += deathsPmin;
				totalAssistspM += assistsPmin;
				if(win)
				{winRate++;}
				exampleSize++;
				
			}
			
			//********VALUES TO DB***********
			totalGoldpM /= exampleSize;
			totalKillspM /= exampleSize;
			totalDeathspM /= exampleSize;
			totalAssistspM /= exampleSize;
			winRate /= exampleSize;
			exampleSize += 0;
			//********VALUES TO DB **********
			DBCollection championz = riotData.getCollection("champions");
			DBObject updateChamp = new BasicDBObject();
			updateChamp.put("id", champ);
			DBObject replace = championz.findOne(updateChamp);
			updateChamp = new BasicDBObject();
			updateChamp.put("goldPmin", totalGoldpM);
			updateChamp.put("killsPmin", totalKillspM);
			updateChamp.put("deathsPmin", totalDeathspM);
			updateChamp.put("assistsPmin", totalAssistspM);
			updateChamp.put("winRate", winRate);
			updateChamp.put("exampleCount", exampleSize);
			championz.update(replace, new BasicDBObject ("$set",updateChamp));
			System.out.println(championz.findOne(new BasicDBObject("id",champ)));
			//summoners.update(possible, new BasicDBObject( "$set", new BasicDBObject("crawl", VERSION)));
			
		}
	}
	
	private static DBCursor findByEmbedObj(String key, Object val)
	{
		DBCollection games = riotData.getCollection("games");
		
		DBObject query = new BasicDBObject();
		query.put("gameStats."+key, val);
		
		return games.find(query);
		
	}
	
}
