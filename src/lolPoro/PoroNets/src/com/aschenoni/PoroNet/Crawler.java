package com.aschenoni.PoroNet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class Crawler {
	
	private static final String NA = "na";
	static MongoClient client;
	private static int minute;
	private static int second;
	private static GameConstants constants;
	private static Node currentNode;
	private static int oldSummonerSize = 0;
	private static final String VERSION = "v1.2";
	private static int skipped =0;
	
	public static void init() throws MalformedURLException, IOException
	{
		try {
			client = new MongoClient("localhost", 27017);
			constants = new GameConstants(client);
			currentNode = constants.getHead();
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException, JSONException
	{
		init();
		DB riotData = client.getDB("RiotData");
		DBCollection summoners = riotData.getCollection("summoners");
		DBCollection games = riotData.getCollection("games");
		DBCollection blackListGames = riotData.getCollection("blackListGames");
		DBCollection gamesBySummoner = riotData.getCollection("gamesBySummoner");


		summonerCrawl(summoners,5000);
		//gameCrawl(games, summoners, -1);
	}
	
	/**
	 * Expand the summoners database by pulling from summoners recent games
	 * 
	 * @param summoners the database of summoners to pull from
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws InterruptedException 
	 */
	public static void summonerCrawl(DBCollection summoners, int maxSummoners) throws MalformedURLException, IOException, InterruptedException

	{
		//initialize a set of summoners that we can iterate over to grab more summoners from their recent games
		List<Integer> activeSummonerSet = new ArrayList<Integer>();
		
		//add all the summoners we have from the database
		DBCursor cursor = summoners.find();
		List<DBObject> cursor1 =cursor.toArray();

		if(cursor1.isEmpty())
		{
			addProfileToDB(20961388);
			cursor = summoners.find();
			cursor1 = cursor.toArray();
		}
		
		for(DBObject summoner: cursor1)
		{
			int id = (int) summoner.get("id");
			if(!(summoner.get("crawl").equals(VERSION)))
			{
				activeSummonerSet.add(id);
				System.out.println("Adding "+id +" to the initial list");
			}
			else
			{
				System.out.println("skipping " + id);
				skipped++;
			}
		}
		
		System.out.println("skipped " + skipped + " many summoners because they were recently crawled");
		Thread.sleep(4000);
		int i = 0;
		while(i < activeSummonerSet.size())
		{
			BasicDBObject query = new BasicDBObject();
			query.put("id", activeSummonerSet.get(0) );
			DBObject possible = summoners.findOne(query);
			
			//if summoner = already crawled then skip it.

			
			BasicDBList gameStats = getSummonerGames(activeSummonerSet.get(0) );
			query = new BasicDBObject();
			query.put("id", activeSummonerSet.remove(0));
			possible = summoners.findOne(query);
			summoners.update(possible, new BasicDBObject( "$set", new BasicDBObject("crawl", VERSION)));
		
			for ( Object game: gameStats)
			{
				List<Integer> summonersInGame = getSummonersInGame((DBObject)game);
				handleTime();
				for(Integer summoner : summonersInGame)
				{
					
					DBObject query1 = new BasicDBObject();
					query1.put("id", summoner);
					
					if(summoners.findOne(query1) == null )
					{
						//activeSummonerSet.add(summoner); //too many summoners in the active list as of now
						addProfileToDB(summoner);
						handleTime();
					}
				}
						
			}
			if(activeSummonerSet.size() % 10 == 0)
			{
				System.out.println("list size is now: " + activeSummonerSet.size() );
			}
			else if (i%50 == 0)
			{
				System.out.println("This was iteration #" + i);
			}
			else
			{
				System.out.print(".");
			}
			
				i++;
		}
	}
	
	/**
	 * Expand the games list by pulling recent games from summoners in the summoners database
	 * @param games
	 * @param summoners
	 * @param maxSummoner
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static void gameCrawl(DBCollection games, DBCollection summoners, int maxSummoner) throws InterruptedException, IOException, JSONException
	{
		//games database consists of documents with the following attributes
		//GameID: int, gametype: int, gameStats: List[GameDto] date: date complete: bool
		
		
		//create a queue of summoners
		List<Integer> activeSummonerSet = new ArrayList<Integer>();
		
		DBCursor cursor = summoners.find();
		List<DBObject> cursor1 =cursor.toArray();

		if(cursor1.isEmpty())
		{ 
			addProfileToDB(20961388);
			cursor = summoners.find();
			cursor1 = cursor.toArray();
		}
		
		for(DBObject summoner: cursor1)
		{
			int id = (int) summoner.get("id");
			if(!(summoner.get("crawl").equals(VERSION)))
			{
				activeSummonerSet.add(id);
			}
			else
			{
				System.out.println("skipping " + id);
				skipped++;
			}
		}

		
		int i = 0;
		while(i != maxSummoner) //for the current summoner:
		{
			Integer currentSummoner = activeSummonerSet.remove(0);
			
			BasicDBObject query1 = new BasicDBObject();
			query1.put("id", currentSummoner);
			DBObject possible = summoners.findOne(query1);
			summoners.update(possible, new BasicDBObject( "$set", new BasicDBObject("crawl", VERSION)));
			
			///check to see if he has ranked games
			BasicDBList currentSummonerGames = getSummonerGames(currentSummoner);
			handleTime();
			
			for(Object object: currentSummonerGames)
			{
				DBObject currentGame = (DBObject) object;
				String gameType = (String) currentGame.get("subType");
				if(gameType.equals("RANKED_SOLO_5x5")) ///if you find a ranked game:
				{
					Integer gameId = (Integer) currentGame.get("gameId");
					String currentPlayer = currentSummoner.toString();
					
					DBObject insertable = new BasicDBObject();
					insertable.put("gameId", gameId);
					insertable.put("summonerId", currentSummoner);
					insertable.put("createDate", currentGame.get("createDate"));
					insertable.put("gameStats", currentGame);
					
					BasicDBObject query = new BasicDBObject();
					query.put("gameId", gameId);
					query.put("summonerId", currentSummoner);
					DBObject gameAlreadyListed = games.findOne(query);
					if(gameAlreadyListed == null) //the object isn't in the DB already
					{
						games.insert(insertable);
					}
					else
					{
						System.out.print(",");
					}
					
//					BasicDBObject potentialRankedGame = new BasicDBObject();
//					potentialRankedGame.append(currentPlayer, currentGame);

//					List<Integer> fellowPlayers = new ArrayList<Integer>(); ////create a temp array of the other 9 summoners
//					BasicDBList playersInGame = (BasicDBList) currentGame.get("fellowPlayers");
//					for(int j = 0; j < 9; j++)
//					{
//						BasicDBObject player = (BasicDBObject) playersInGame.get(j);
//						fellowPlayers.add( (Integer) player.get("summonerId"));
//					}
					
					
//					for(Integer player: fellowPlayers)
//					{
//						BasicDBList candidateGamesList = getSummonerGames(player);
//						handleTime();
//						
//						for(Object game: candidateGamesList) ////attempt to build the full data for that ranked game -- create a list of gamestats, add the first game stats
//						{
//							DBObject gameObject = (DBObject) game;
//							Integer game2 = (Integer) gameObject.get("gameId");
//							if(game2.equals(gameId) )
//							{
//								BasicDBObject temp4 = new BasicDBObject();
//								temp4.put("summonerId", player);
//								potentialRankedGame.append(player.toString(), game);
//							}
//						}
//						
//					}
//					
//					if (potentialRankedGame.size() == 10 )
//					{
//						BasicDBObject query = new BasicDBObject();
//						query.put("gameId", gameId);
//						DBObject gameAlreadyListed = games.findOne(query);
//						if(gameAlreadyListed == null) //the object isn't in the DB already
//						{
//							System.out.println("gameId: " + gameId + " is not in the DB!");
//							BasicDBObject insertable = new BasicDBObject();
//							insertable.put("gameId", gameId);
//							int summonerNumber = 1;
//							for(Map.Entry rankedGame: potentialRankedGame.entrySet())
//							{
//								BasicDBObject indivStat = new BasicDBObject();
//								String thisSummoner = "Summoner"+summonerNumber;
//								String thisSummonerStats = thisSummoner + "Stats";
//								BasicDBObject temp3 = new BasicDBObject();
//								insertable.put(thisSummoner, rankedGame.getKey() );
//								insertable.put(thisSummonerStats, rankedGame.getValue() );
//								
//								
//								summonerNumber++;
//								
//							}
//							System.out.println("This will be inserted into the DB: " + insertable);
//							
//						}
//					}
//					else
//					{
//						System.out.println("game " + gameId + " was not a complete game...");
//					}
					
				}
			}
			
			
			
			if(i%50 == 0)
			{
				System.out.println("We have searched through " + i + " summoners");	
			}
			if(i%250 == 0)
			{
				System.out.println("summoner list is at " + activeSummonerSet.size());
			}
			else
			{
				System.out.print(".");
			}

			i++;
		}
		
		
		////check each of their games to see if that game id is present
		////add that game data to the array of gamestats
		////if the data is absent for 1 out of the 10 summoners, dump the array and move to the next game
	}

	private static List<Integer> getSummonersInGame(DBObject game)
	{
		List<Integer> summonerIds = new ArrayList<Integer>();
		
		BasicDBList fellowPlayers = (BasicDBList) game.get("fellowPlayers");
		if (fellowPlayers != null)
		{
			for(int i = 0; i < fellowPlayers.size(); i++)
			{
				DBObject temp = (DBObject) fellowPlayers.get(i);
				summonerIds.add((Integer) temp.get("summonerId"));
			}
		}		
		return summonerIds;
	}
	
	private static void tagSummonerAsProfileCrawled(Integer summonerId)
	{
		//refactor
	}
	
	private static void tagSummonerAsGameCrawled(Integer summonerId)
	{
		//refactor
	}
	
	private static void addSummonerGamesToDB(Integer summonerId, BasicDBList games)
	{
		//refactor
	}
	
	private static BasicDBList getSummonerGames(Integer id) throws InterruptedException, IOException
	{
		String gameList = URLmaker.game(NA, id, currentNode.value() );
		try {
			gameList = IOUtils.toString(new URL(gameList));
			currentNode.actionsSec++;
			currentNode.actions1++;

		} catch (IOException e) {
			System.out.println("#####################HTTP response code 429#######################");
			Thread.sleep(10000);
			currentNode = currentNode.next();
			gameList = IOUtils.toString(new URL(gameList));
			currentNode.actionsSec++;
			currentNode.actions1++;
		}
		
		DBObject games = (DBObject) JSON.parse(gameList);
		BasicDBList gameStats = (BasicDBList) games.get("games");
		
		return gameStats;
	}
	
	private static void addProfileToDB(Integer id) throws InterruptedException, IOException
	{
		DB riotDB = client.getDB("RiotData");
		DBCollection summoners = riotDB.getCollection("summoners");
		
		String summonerProfile = URLmaker.summoner(NA, id, currentNode.value() );
		
		try {
			summonerProfile = IOUtils.toString(new URL(summonerProfile));
			currentNode.actionsSec++;
			currentNode.actions1++;

		} catch (IOException e) {
			System.out.println("#####################HTTP response code 429#######################");
			Thread.sleep(10000);
			e.printStackTrace();
			currentNode = currentNode.next();
			summonerProfile = IOUtils.toString(new URL(summonerProfile));
			currentNode.actionsSec++;
			currentNode.actions1++;
		}

		DBObject profile = (DBObject) JSON.parse(summonerProfile);
		profile.put("crawl", "");
		summoners.insert(profile);
		
	}
	
	public static void handleTime() throws InterruptedException
	{

		second = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() );
		minute = (int) (System.currentTimeMillis() /1000 / 60)% 100;
		if (currentNode.currentSec + 10 <= second)
		{
			currentNode.currentSec = second;
			currentNode.actionsSec = 0;
		}
		if (currentNode.actionsSec >= 6)
		{
			currentNode = currentNode.next();
		}
		
		if (currentNode.currentMin > 60)
		{
			minute -= 60;
		}
		
		if( (currentNode.currentMin + 1) <= minute)
		{
			currentNode.currentMin = minute;
			currentNode.actions1 = 0;
		}
		if( currentNode.actions1 >= 50)
		{
			currentNode = currentNode.next();
		}
		return;
	}
}
