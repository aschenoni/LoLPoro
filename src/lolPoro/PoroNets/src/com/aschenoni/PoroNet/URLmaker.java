package com.aschenoni.PoroNet;
import java.util.HashMap;
/**
 * @author abjoy_000
 *
 */
public class URLmaker {
	
	//commonly used url building blocks
	static String urlStub = "http://prod.api.pvp.net/api/";
	
	//API CURRENT VERSIONS
	HashMap<String,String> apiVersions = new HashMap<String, String>();
	//throw these in a text file later to be moved into map
	//that way we make sure it stays up to date
	//TODO
	private static String champV = "1.1";
	private static String gameV = "1.2";
	private static String leagueV = "2.2";
	private static String statsV = "1.2";
	private static String summonerV = "1.2";
	private static String teamV = "2.2";
	
	/**
	 * creates the URL to query Riot servers for a list of champions
	 * 
	 * @param server - NA, EUNE, EUW
	 * @param freeChamps - whether or not you only want to see this weeks
	 * 					   free to play champions
	 * @param key - the key to use for lookup
	 * @return Champion[] in JSON form //TODO Champion Class
	 */
	public static String champion(String server, boolean freeChamps, String key) 
	{
		String url = urlStub + "lol/"+ server + "/v" + champV + "/";
		if (freeChamps)
			return  url + "champion?freeToPlay=true&api_key=" + key;
		else
			return url + "champion?api_key=" + key;
	}
	
	/**
	 * creates the URL to query Riot servers for a list of recent games
	 * played by a specific summoner
	 * 
	 * @param server - NA, EUNE, EUW
	 * @param summonerID - ID of the person's games to see
	 * @param key - the key to use for lookup
	 * @return Game[],summonerID in JSON form //TODO Class
	 */
	public static String game(String server, int summonerID, String key)
	{
		String url = urlStub + "lol/" + server + "/v" + gameV + "/game/by-summoner/" + summonerID + "/recent?api_key=" + key;
		return url;
	}
	
	/**
	 * creates the URL to query Riot servers for a list of leagues
	 * that a summoner has any involvement with
	 * 
	 * @param server - NA, EUNE, EUW, TR, BR
	 * @param summonerID - ID of the person's leagues to see
	 * @param key - the key to use for lookup
	 * @return League[] in JSON form //TODO League[] Class
	 */
	public static String league(String server, int summonerID, String key)
	{
		String url = urlStub + server + "/v" + leagueV + "/league/by-summoner/" + summonerID + "?api_key=" + key ;
		return url;
	}
	
	/**
	 * creates the URL to query Riot servers for a list of stats on a summoner
	 * 
	 * @param server - NA, EUNE, EUW
	 * @param summonerID - ID of the person's leagues to see
	 * @param season - the number of the season of interest
	 * @param ranked - if true return only stats for ranked games
	 * @param key - the key to use for lookup
	 * @return Summoner.Stats in JSON form
	 */
	public static String stats(String server, int summonerID ,int season, boolean ranked, String key)
	{
		String url = urlStub + "lol/" + server + "/v" + statsV + "/stats/by-summoner/" + summonerID;
		if (ranked)
			url = url + "/ranked?season=SEASON" + + season + "&api_key=" + key;
		else	
			url = url + "/summary?season=SEASON" + season + "&api_key=" + key;
		return url;
	}
	
	/**
	 * creates the URL to query Riot servers for runes or masteries
	 * for a specific summoner
	 * 
	 * @param server - NA, EUNE, EUW
	 * @param summonerID - ID of the person's info of interest
	 * @param runes - queries for runes if true queries for masteries if false
	 * @param key - the key to use for lookup
	 * @return Runes[] or Masteries[] in JSON form depending on runesOrMast //Runes Class Masteries Class 
	 */
	public static String summonerInfo(String server, int summonerID, boolean runesOrMast, String key)
	{
		String url = urlStub + "lol/" + server + "/v" + summonerV + "/summoner/" + summonerID + "/";
		if (runesOrMast)
			url = url + "masteries?api_key=" + key;
		else
			url = url + "runes?api_key=" + key;
		return url;
	}
	
	/**
	 * creates the URL to query Riot servers for a summoner's basic info
	 * using the name of the summoner
	 * @param server - NA, EUNE, EUW
	 * @param summonerName - name of person of interest
	 * @param key - the key to use for lookup
	 * @return Summoner in JSON form //TODO Summoner Class
	 */
	public static String summoner(String server, String summonerName, String key)
	{
		String url = urlStub + "lol/" + server + "/v" + summonerV +"/summoner/by-name/" + summonerName + "?api_key=" + key;
		return url;
	}
	
	/**
	 * creates the URL to query Riot servers for a summoner's basic info
	 * using the name of the summoner
	 * @param server - NA, EUNE, EUW
	 * @param summonerID - ID of person of interest
	 * @param key - the key to use for lookup
	 * @return Summoner in JSON form
	 */
	public static String summoner(String server, int summonerID, String key)
	{
		String url = urlStub + "lol/" + server + "/v" + summonerV +"/summoner/" + summonerID + "?api_key=" + key;
		return url;
	}
	
	public static String listOfSummoners()
	{
		//TODO implement this method
		//implement later due to JSON list parsing needs
		return null;
	}
	
	/**
	 * creates the URL to query Riot servers for a summoner's team's info
	 * 
	 * @param server - NA, EUNE, EUW, BR, TR
	 * @param summonerID - ID of person of interest
	 * @param key - the key to use for lookup
	 * @return Team[] in JSON form //TODO Team Class
	 */
	public static String team(String server, int summonerID, String key)
	{
		String url = urlStub + server + "/v" + teamV + "/team/by-summoner/" + summonerID + "?api_key=" + key;
		return url;
	}
}
