package ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Summoner {
	private String name;
	private int id;
	private int champion;
	private String champName;
	private int team;
	private String rank = "Unranked";
	private String division = "";
	private int[] rCount;
	private int[] rPage;
	private int wins;
	private int losses;
	public static JsonObject rData;
	private String style;
	private String rankIcon;
	private String lp = "";

	public void setName(String str) {
		name = str;
	}

	public void setLP(String str){
		lp = str;
	}

	public void setRank(String str) {
		rank = str;
	}

	public void setRankIcon(String str) {
		rankIcon = str;
	}

	public void setDivision(String str) {
		division = str;
	}

	public void setID(int n) {
		id = n;
	}

	public void setWins(int n) {
		wins = n;
	}

	public void setLosses(int n) {
		losses = n;
	}

	public void setChampion(int n) {
		champion = n;
	}

	public void setTeam(int n) {
		team = n;
	}

	public void setRuneCount(int[] n) {
		rCount = n;
	}

	public void setRunePage(int[] a) {
		rPage = a;
	}

	public void runeAggro() {
		double aggressive = 0;
		double passive = 0;
		for (int i = 0; i < rPage.length; i++) {
			JsonObject rInfo = rData.get(Integer.toString(rPage[i])).getAsJsonObject();
			// gets player's first rune stored in array
			JsonArray tags = rInfo.get("tags").getAsJsonArray();
			// gets tag for the stored rune
			double temp = 0;
			for (int k = 0; k < tags.size(); k++) {
				// tag checks for each type of rune
				// JsonObject tagData = tags.get(k).getAsJsonObject();
				String tag = tags.get(k).getAsString();
				if (tag.equals("quintessence")) {
					temp *= 3;
					// quintessences are about 3 times as powerful as other
					// runes
				} else if (tag.equals("physicalAttack") || tag.equals("magic")) {
					temp = rCount[i];
					aggressive += temp;
				} else if (tag.equals("defense")) {
					temp = rCount[i];
					passive += temp;
				} else if (tag.equals("utility")) {
					temp = 0.5 * rCount[i];
					// general utility runes favor offense a bit hence the 0.5
					// multiplier
					aggressive += temp;
				} else {

				}

				// defense, physical attack, utility, magic
				// scaling/flat
				// quint blue red yellow
			}
		}
		if (Math.abs(aggressive - passive) <= 3) {
			style = "balanced";
		} else if (aggressive - passive > 3) {
			style = "aggressive";
		} else {
			style = "passive";
		}
	}

	public String getName() {
		return name;
	}

	public int getID() {
		return id;
	}

	public String getLP(){
		return lp;
	}

	public String getChampName() {
		return champName;
	}

	public int getTeam() {
		return team;
	}

	public String getRank() {
		return rank;
	}

	public String getDivision() {
		return division;
	}

	public String getStyle() {
		return style;
	}

	public int getLosses() {
		return losses;
	}

	public int getWins() {
		return wins;
	}

	public String getRankIcon(){
		return rankIcon;
	}
	public void findChampName() throws IOException {
		URL url = new URL("https://global.api.pvp.net/api/lol/static-data/na/v1.2/champion/" + champion + "?api_key="
				+ search.key);

		HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
		try {
			request.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("does not exist");
		}
		JsonParser js = new JsonParser();
		JsonElement champ = js.parse(new InputStreamReader((InputStream) request.getContent()));
		JsonObject champObj = champ.getAsJsonObject();
		champName = champObj.get("name").getAsString();
	}
}
