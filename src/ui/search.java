package ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.wb.swt.SWTResourceManager;

public class search {

	protected Shell shell;
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
	private Text summoner;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */

	// static so I can access this in all methods
	static String key = "RGAPI-b487d701-c19c-4496-9f5c-d1257edda7e2";
	static Summoner[] summArray;

	public static void main(String[] args) {
		try {
			search window = new search();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		// shell.setImage(SWTResourceManager.getImage(search.class, "/ui/launchIcon.png"));
		// make sure image is in the same folder
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		Display display = Display.getDefault();
		shell = new Shell();
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		shell.setMinimumSize(new Point(750, 620));
		shell.setSize(750, 620);
		shell.setText("IA LoL Search");
		shell.setLayout(new GridLayout(6, false));

		Label lblName = new Label(shell, SWT.NONE);
		GridData gd_lblName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblName.widthHint = 34;
		lblName.setLayoutData(gd_lblName);
		formToolkit.adapt(lblName, true, true);
		lblName.setText("Name");

		summoner = new Text(shell, SWT.BORDER);
		summoner.setToolTipText("Enter a Summoner in a game");
		GridData gd_summoner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_summoner.widthHint = 100;
		summoner.setLayoutData(gd_summoner);
		formToolkit.adapt(summoner, true, true);

		Button search = new Button(shell, SWT.NONE);
		search.setToolTipText("Search");
		formToolkit.adapt(search, true, true);
		search.setText("Search");
		shell.setDefaultButton(search); // ensures that enter triggers search

		Label lblRegion = new Label(shell, SWT.NONE);
		lblRegion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		formToolkit.adapt(lblRegion, true, true);
		lblRegion.setText("Region");

		Combo region = new Combo(shell, SWT.READ_ONLY);
		region.setItems(new String[] { "BR", "EUNE", "EUW", "JP", "KR", "LAN", "LAS", "NA", "OCE", "RU", "TR" });
		GridData gd_region = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_region.widthHint = 30;
		region.setLayoutData(gd_region);
		formToolkit.adapt(region);
		formToolkit.paintBordersFor(region);
		region.setText("NA");

		ProgressBar progressBar = new ProgressBar(shell, SWT.INDETERMINATE);
		GridData gd_progressBar = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_progressBar.widthHint = 153;
		progressBar.setLayoutData(gd_progressBar);
		formToolkit.adapt(progressBar, true, true);
		progressBar.setVisible(false);

		Canvas canvas = new Canvas(shell, SWT.NONE);
		canvas.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		canvas.setLayout(null);
		GridData gd_canvas = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
		gd_canvas.widthHint = 299;
		canvas.setLayoutData(gd_canvas);
		formToolkit.adapt(canvas);
		formToolkit.paintBordersFor(canvas);

		Menu menu = new Menu(shell, SWT.BAR);
		menu.setLocation(new Point(0, 0));
		shell.setMenuBar(menu);

		GC gc = new GC(canvas);
		gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		// downloads the static data to speed things up since it's the same for
		// all players
		// rune stuff starts here
		try {
			URL url = new URL(
					"https://global.api.pvp.net/api/lol/static-data/na/v1.2/rune?runeListData=tags&api_key=" + key);
			HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
			request.connect();
			JsonParser js = new JsonParser();
			JsonElement runes = js.parse(new InputStreamReader((InputStream) request.getContent()));
			JsonObject rInfo = runes.getAsJsonObject();
			JsonObject rData = rInfo.get("data").getAsJsonObject();
			Summoner.rData = rData;
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			gc.drawString("Could not download static data - runepages will not be shown", 10, 10);
		}

		search.addSelectionListener(new SelectionAdapter() { // main thread
			@Override
			public void widgetSelected(SelectionEvent e) {
				progressBar.redraw();
				progressBar.setVisible(true);
				String name = summoner.getText();
				String searchRegion = region.getText();
				canvas.redraw(); // find a way to clear everything

				canvas.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						GC gc = new GC(canvas);
						gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
						gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
						gc.fillRectangle(canvas.getBounds()); // "clears" canvas
						gc.fillRectangle(display.getBounds());
						if (verifyName(name)) {
							int id;
							try {
								id = nameToID(name, searchRegion);
							} catch (NullPointerException | IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								id = -1;
							}

							if (id == -1) {
								gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
								gc.drawString("Summoner does not exist", 0, 0);
							} else {
								gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
								// gc.drawString(Integer.toString(id), 0, 0);

								if (gameInfo(id, searchRegion).length == 1) {
									gc.drawString("Summoner is not in an active game", 0, 0);
								} else {
									gameInfo(id, searchRegion);
									// test stuff
									for (int i = 0; i < 10; i++) {
										summArray[i].runeAggro();
										try {
											summArray[i].findChampName();
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
											gc.drawString("Champion name could not be found", 10, 10);
										}
									}

									try {
										findRank(region.getText());
										// kinda bugged
									} catch (IOException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
										gc.drawString("Riot's API may be down", 325, 0);
									}

									for (int i = 0; i < 10; i++) {
										summArray[i].runeAggro();
										try {
											summArray[i].findChampName();
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											gc.drawString("Could not find champion name", 150, 35);
										}
									}
									gc.drawString("Blue Team", 350, 10);

									for (int i = 0; i < 5; i++) {
										if (summArray[i].getTeam() == 100) {
											gc.drawString(summArray[i].getName(), 15 + 150 * i, 30);
											gc.drawString(summArray[i].getChampName(), 15 + 150 * i, 50);
											gc.drawString(summArray[i].getRank() + " " + summArray[i].getDivision() + "  "
													+ summArray[i].getLP() + " LP", 15 + 150 * i, 70);
											gc.drawString(
													summArray[i].getWins() + "W  " + summArray[i].getLosses() + "L",
													15 + 150 * i, 90);
											gc.drawString(summArray[i].getStyle(), 15 + 150 * i, 110);
											gc.drawImage(
													SWTResourceManager.getImage(search.class,
															"/ui/tier_icons/" + summArray[i].getRankIcon() + ".png"),
													148 * i, 120);
										}
									}
									gc.drawString("Red Team", 350, 260);
									for (int i = 5; i < 10; i++) {
										if (summArray[i].getTeam() == 200) {
											gc.drawString(summArray[i].getName(), 15 + 150 * (i - 5), 280);
											gc.drawString(summArray[i].getChampName(), 15 + 150 * (i - 5), 300);
											gc.drawString(summArray[i].getRank() + " " + summArray[i].getDivision()+ "  "
													+ summArray[i].getLP() + " LP", 15 + 150 * (i - 5), 320);
											gc.drawString(
													summArray[i].getWins() + "W  " + summArray[i].getLosses() + "L", 15 + 150 * (i - 5), 340);
											gc.drawString(summArray[i].getStyle(), 15 + 150 * (i - 5), 360);
											gc.drawImage(
													SWTResourceManager.getImage(search.class,
															"/ui/tier_icons/" + summArray[i].getRankIcon() + ".png"),
													148 * (i - 5), 370);
										}
									}
								}
							}

						} else {
							gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
							gc.drawString("Please enter a valid summoner name", 0, 0);
						}
					}
				});
				// hides the progress bar after action is complete
				progressBar.setSelection(50);
				progressBar.setVisible(false);
			}
		});
	}

	public static boolean verifyName(String name) {
		if (name.length() < 3 || name.length() > 16) {
			return false;
		} else {
			for (int i = 0; i < name.length(); i++) {
				if (name.codePointAt(i) == 33) {
					return false;
				}
			}
		}
		return true;
	}

	public static int nameToID(String name, String region) throws IOException, NullPointerException {
		String noSpaceName = name.replaceAll("\\s+", "").toLowerCase();
		URL url = new URL("https://" + region + ".api.pvp.net/api/lol/" + region + "/v1.4/summoner/by-name/"
				+ noSpaceName + "?api_key=" + key);
		HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
		try {
			request.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("does not exist");
		}
		JsonParser js = new JsonParser();
		JsonElement summoner = null;
		int id = -1; // if an ioexception occurs, it returns -1, which cannot be
						// any summoner id, signifies that summoner does not
						// exist
		try {
			summoner = js.parse(new InputStreamReader((InputStream) request.getContent()));
			JsonObject summObj = summoner.getAsJsonObject();
			JsonElement summInfo = summObj.get(noSpaceName);
			// gets the JSON w/o the name, prevents getting null id
			JsonObject summID = summInfo.getAsJsonObject();
			id = summID.get("id").getAsInt();
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			System.out.println("summoner does not exist");
		}
		return id;
	}

	public static Summoner[] gameInfo(int searchID, String region) {
		String cGameRegion; // stores region needed for current game search

		if (region != "EUNE" || region != "OCE") {
			cGameRegion = region + 1;
		} else {
			cGameRegion = region.substring(0, region.length() - 1) + 1;
			// gets correct region for eune and oce
		}
		try {
			URL url = new URL("https://" + region + ".api.pvp.net/observer-mode/rest/consumer/getSpectatorGameInfo/"
					+ cGameRegion + "/" + searchID + "?api_key=" + key);
			HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
			request.connect();
			JsonParser js = new JsonParser();
			JsonElement cGame = js.parse(new InputStreamReader((InputStream) request.getContent()));
			JsonObject gameObj = cGame.getAsJsonObject();
			JsonArray participants = gameObj.get("participants").getAsJsonArray();

			summArray = new Summoner[10];
			for (int i = 0; i < 10; i++) {

				JsonObject partInfo = participants.get(i).getAsJsonObject();
				JsonArray runeInfo = partInfo.get("runes").getAsJsonArray();

				// following code gets information about the players
				summArray[i] = new Summoner();
				summArray[i].setName(partInfo.get("summonerName").getAsString());
				summArray[i].setTeam(partInfo.get("teamId").getAsInt());
				summArray[i].setID(partInfo.get("summonerId").getAsInt());
				summArray[i].setChampion(partInfo.get("championId").getAsInt());
				int[] rCount = new int[runeInfo.size()];
				int[] rPage = new int[runeInfo.size()];
				for (int k = 0; k < runeInfo.size(); k++) {
					JsonObject runeTemp = runeInfo.get(k).getAsJsonObject();
					rCount[k] = runeTemp.get("count").getAsInt();
					rPage[k] = runeTemp.get("runeId").getAsInt();
					// finds runes for an individual player
				}

				summArray[i].setRuneCount(rCount);
				summArray[i].setRunePage(rPage);
				// System.out.println(name);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			summArray = new Summoner[1];
			System.out.println("Summoner is not in a current game");
		}
		return summArray;
	}

	public static void findRank(String region) throws IOException {
		String searchID = Integer.toString(summArray[0].getID());

		for (int i = 1; i < 10; i++) {
			searchID += ",";
			searchID += Integer.toString(summArray[i].getID());
			// gets all the ids to be searched into one string so that I don't
			// get banned for exceeding rate limits from the api
		}

		// uncomment when riot fixes
		URL url = new URL("https://" + region + ".api.pvp.net/api/lol/" + region + "/v2.5/league/by-summoner/"
				+ searchID + "/entry" + "?api_key=" + key);
		HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
		try {
			request.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("does not exist");
		}
		JsonParser js = new JsonParser();

		JsonElement summoner = js.parse(new InputStreamReader((InputStream) request.getContent()));
		JsonObject summObj = summoner.getAsJsonObject();
		// gets the entire JSON file returned by api
		// change this back when riot fixes
		for (int i = 0; i < 10; i++) {
			try {
				JsonElement summInfo = summObj.get(Integer.toString(summArray[i].getID()));
				// gets the information for the specified summoner
				JsonArray summStats = summInfo.getAsJsonArray();
				JsonObject summBase = summStats.get(0).getAsJsonObject();
				summArray[i].setRank(summBase.get("tier").getAsString().toLowerCase());
				summArray[i].setRank(
						summArray[i].getRank().substring(0, 1).toUpperCase() + summArray[i].getRank().substring(1));
				// makes the rank more visually appealing by only having the
				// first character capitalized
				JsonArray summLeagueStats = summBase.get("entries").getAsJsonArray();
				JsonObject rankedStats = summLeagueStats.get(0).getAsJsonObject();
				// for some reason riot passes this info as a JSON Array with 1
				// JSON Object
				summArray[i].setDivision(rankedStats.get("division").getAsString());
				summArray[i].setWins(rankedStats.get("wins").getAsInt());
				summArray[i].setLosses(rankedStats.get("losses").getAsInt());
				summArray[i].setLP(rankedStats.get("leaguePoints").getAsString());
				summArray[i].setRankIcon((summArray[i].getRank() + "_" + summArray[i].getDivision()).toLowerCase());
			} catch (JsonIOException | JsonSyntaxException e) {
				// TODO Auto-generated catch block
				// System.out.println("summoner does not exist");
				summArray[i].setRank("unranked");
				summArray[i].setDivision("i");
			}
			if (summArray[i].getRank().equals("null")) {
				summArray[i].setRank("unranked");
			}
			if (summArray[i].getDivision().equals("null")) {
				summArray[i].setDivision("i");
			}
			// handles cases where summoner has no ranked info, sets up as
			// unranked

		}

	}
}
