package Views;

import Controllers.GameController;
import Controllers.MenuController;
import Models.GameClient;
import Models.RMIInterface;
import Models.Speler;
import Models.TileStump;
import commonFunctions.Point;
import commonFunctions.SmartButton;
import commonFunctions.SmartLabel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Deze class zorgt ervoor dat het daadwerkelijke speelbord goed wordt weergegeven.
 */
public class GameScene extends Scene {

	private double xOffset;
	private double yOffset;
	private int sceneHeight = (int) getHeight();
	private int sceneWidth = (int) getWidth();
	private MenuController controller;
	public GameController gameController;
	private BorderPane mainPane;
	private Pane tilesPane;
	private HBox test;
	private TileView[][] tileViews;
	private HorigeView[] horigeViews;
	private SpelerView[] playerViews;
	private ImageView ShowKaart;
	private String kaartPlaatsId;
	private double scale;
	public RMIInterface RmiStub;
	private SmartLabel KaartenLeft;
	private SmartButton menuButton;
	private	Point laatstGeplaatstLocatie;
	private SmartButton eindigButton;
	private SmartButton draaiButton;
	private HBox onderkant;
	private VBox links;

	/**
	 * Constructor van de GameScene
	 *
	 * @param menuController Geef MenuController mee
	 */
	public GameScene(MenuController menuController) {
		//	super(new Pane(), 1280, 720);
		super(new Pane(), 1260, 700);
		getStylesheets().add("style.css");
		tilesPane = (Pane) this.getRoot();

		this.controller = menuController;

		createTileGrid(100, 100);

		init();
	}

	private void init() {
		// new shit
		mainPane = new BorderPane();
		links = new VBox(5);
		onderkant = new HBox();
		draaiButton = new SmartButton("Draaien");
		eindigButton = new SmartButton("Beëindig beurt");
		menuButton = new SmartButton("Menu");
		playerViews = new SpelerView[5];
		ShowKaart = new ImageView();
		KaartenLeft = new SmartLabel("Kaarten over: 72");

		// id
		mainPane.setId("test");
		draaiButton.setId("standardLabel");
		eindigButton.setId("standardLabel");
		menuButton.setId("standardLabel");
		ShowKaart.setId("Kaartview");
		KaartenLeft.setId("standardLabel");

		//Size BorderPane
		mainPane.setPrefSize(1280, 720);
		mainPane.setMaxSize(1280, 720);
		mainPane.setMinSize(1280, 720);

		//Set PickOnBounds
		mainPane.setPickOnBounds(false);
		links.setPickOnBounds(false);
		onderkant.setPickOnBounds(false);

		// Set padding
		onderkant.setPadding(new Insets(0, 0, 0, 0));

		//Prop
		menuButton.minHeightProperty().bind(heightProperty().multiply(0.2));
		menuButton.minWidthProperty().bind(widthProperty().multiply(0.11));
		ShowKaart.fitHeightProperty().bind(heightProperty().multiply(0.2));
		ShowKaart.fitWidthProperty().bind(widthProperty().multiply(0.11));

		//GetChilderen
		links.getChildren().add(menuButton);

		//Speler Borden
		for (int i = 0; i < 5; i++) {
			playerViews[i] = new SpelerView();
			playerViews[i].setMinSize(150, 70);
			playerViews[i].setMaxSize(150, 70);
			//playerViews[i].maxHeightProperty().bind(heightProperty().multiply(0.1));
			//playerViews[i].maxWidthProperty().bind(widthProperty().multiply(0.1));
			links.getChildren().add(playerViews[i]);
		}

		links.getChildren().add(ShowKaart);
		links.getChildren().add(KaartenLeft);
		tilesPane.getChildren().add(mainPane);
		onderkant.getChildren().add(links);

		//Horige
		for (int i = 0; i < 7; i++) {
			ImageView horige = new ImageView();
			horige.fitHeightProperty().bind(heightProperty().multiply(0.07));
			horige.fitWidthProperty().bind(horige.fitHeightProperty());
			horige.setId("Horige");
			onderkant.getChildren().add(horige);
		}

		onderkant.getChildren().add(draaiButton);
		onderkant.getChildren().add(eindigButton);




		mainPane.setBottom(onderkant);








		initAction();

	}

	public void initAction(){

		draaiButton.setOnAction(e -> {
			gameController.klikDraaiKaart();
		});

		eindigButton.setOnAction(e -> {
			gameController.klikBeeindigbeurt();
		});

		ShowKaart.setOnMouseClicked(e -> {
			gameController.klikPakKaart();
		});

		menuButton.setOnAction(event -> {
			controller.showInGameMenu();
		});
	}

	/**
	 * Creates the tile grid in the game client
	 *
	 * @param sizeX the x size of the grid (x amount of tiles)
	 * @param sizeY the y size of the grid (y amount of tiles)
	 */
	private void createTileGrid(int sizeX, int sizeY) {
		tileViews = new TileView[sizeX][sizeY];
		VBox verticaal = new VBox();
		for (int y = 0; y < sizeY; y++) {
			HBox horizontal = new HBox();
			verticaal.getChildren().add(horizontal);
			for (int x = 0; x < sizeX; x++) {
				TileView tileView = new TileView(x, y, this);
				tileViews[x][y] = tileView;
				horizontal.getChildren().add(tileView);
			}
		}

		tilesPane.getChildren().add(verticaal);
		tilesPane.setId("spelBordBackground");

		//Verplaatsen over de map met W A S D keys, Speed is de snelheid dat je verplaatst.
		int speed = 20;
		setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.W) {
				verticaal.setLayoutY(verticaal.getLayoutY() + speed);
			} else if (e.getCode() == KeyCode.A) {
				verticaal.setLayoutX(verticaal.getLayoutX() + speed);
			} else if (e.getCode() == KeyCode.S) {
				verticaal.setLayoutY(verticaal.getLayoutY() - speed);
			} else if (e.getCode() == KeyCode.D) {
				verticaal.setLayoutX(verticaal.getLayoutX() - speed);
			} else if (e.getCode() == KeyCode.P) { // Get data
				gameController.saveFileBrowser();
			}
		});

		//Zoom Functie(Scrol event)
		setOnScroll(e -> {
			e.consume();

			if (e.getDeltaY() == 0) {
				return;
			}

			double scaleFactor = (e.getDeltaY() > 0) ? 1.0 : (1 / 1.1);

			tilesPane.setScaleX(tilesPane.getScaleX() * scaleFactor);
			tilesPane.setScaleY(tilesPane.getScaleY() * scaleFactor);

			if (tilesPane.getScaleX() < 1.0) {
				tilesPane.setScaleX(1.0);
			}

			if (tilesPane.getScaleY() < 1.0) {
				tilesPane.setScaleY(1.0);
			}

			if (tilesPane.getScaleX() > 6.0) {
				tilesPane.setScaleX(6.0);
			}

			if (tilesPane.getScaleY() > 6.0) {
				tilesPane.setScaleY(6.0);
			}

		});

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////Eventueel om te draggen. Werkt niet goed met grensen.////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*	tilesPane.setOnMousePressed(e -> {
			xOffset = e.getX();
			yOffset = e.getY();
			e.consume();
		});

		tilesPane.setOnMouseDragged(e -> {
			tilesPane.setTranslateX(e.getX() + tilesPane.getTranslateX() - xOffset);
			tilesPane.setTranslateY(e.getY() + tilesPane.getTranslateY() - yOffset);

			if(tilesPane.getTranslateY() > controller.getGameStage().getMaxHeight()){
				tilesPane.setTranslateY(controller.getGameStage().getMaxHeight());
			}

			if(tilesPane.getTranslateY() < controller.getGameStage().getMinHeight()){
				tilesPane.setTranslateY(controller.getGameStage().getMinHeight());
			}

			if(tilesPane.getTranslateX() > controller.getGameStage().getMaxWidth()){
				tilesPane.setTranslateX(controller.getGameStage().getMaxWidth());
			}

			if(tilesPane.getTranslateX() < controller.getGameStage().getMinWidth()){
				tilesPane.setTranslateX(controller.getGameStage().getMinWidth());
			}

			e.consume();
		});
*/

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}

	/**
	 * Plaatst previews om een tile heen, deze methode mag alleen gerunt worden nadat er een tile geplaatst is
	 *
	 * @param x x co-ordinaat
	 * @param y y co-ordinaat
	 */
	private void addTilePreviews(int x, int y) {
		addTilePreview(x - 1, y);
		addTilePreview(x + 1, y);
		addTilePreview(x, y + 1);
		addTilePreview(x, y - 1);
	}

	/**
	 * Plaatst 1 preview, deze methode mag niet zomaar gerunt worden
	 *
	 * @param x x co-ordinaat
	 * @param y y co-ordinaat
	 */
	private void addTilePreview(int x, int y) {
		if (x < 0 || y < 0) {
			return;
		}
		//Als de tile "Empty" is dan moet er een kaarPreview komen, empty houdt in dat er geen geplaatste tile in zit
		//Deze check is nodig om ervoor te zorgen dat tiles waar al een kaart in zit niet overschreven worden.
		if (tileViews[x][y].getimgId().contains("Empty")) {
			tileViews[x][y].setKaartId("KaartPreview");
		}

	}

	/**
	 * Deze methode laat de kaart draaien
	 */
	public void DraaiKaart() {
		if (ShowKaart.getId().equals("Kaartview")) {
			return;
		}
		ShowKaart.setRotate(ShowKaart.getRotate() + 90);
	}

	/**
	 * Deze functie plaatst de kaart
	 *
	 * @param client Placeholder
	 * @param x      x co-ordinaat
	 * @param y      y co-ordinaat
	 */
	public void plaatsKaart(GameClient client, int x, int y) {
		ShowKaart.setId("Kaartview");
		try {
			TileStump stump = client.getTile();
			laatstGeplaatstLocatie = new Point(stump.getX(), stump.getY());
			tileViews[stump.getX()][stump.getY()].setRotation(stump.getRotation());
			tileViews[stump.getX()][stump.getY()].setKaartId(stump.getId());
			tileViews[x][y].laatHorigePreviewZien(RmiStub.getHorigePosities());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		ShowKaart.setRotate(0);

	}

	public void verwijdwerHorigePreviews() {
		tileViews[laatstGeplaatstLocatie.getX()][laatstGeplaatstLocatie.getY()].verwijderHorigePreviews();
	}

	/**
	 * Deze functie laat de neergelegde kaart zien
	 *
	 * @param client Geef GameClient mee
	 */
	public void showKaart(GameClient client) {
		if (client.kaartPlaatsId == null) {
			ShowKaart.setId("Kaartview");
		} else
			ShowKaart.setId(client.kaartPlaatsId);
	}

	public void removeHorige(int x, int y){
		tileViews[x][y].verwijderHorige();
	}

	int kaartenOver = 0;
	ArrayList<Speler> alleSpelers = null;

	/**
	 * Deze functie zorgt ervoor dat de view wordt geüpdatet
	 *
	 * @param client Geef GameClient mee
	 */
	public void updateView(GameClient client) {
		TileStump stump = null;

		try {
			//Haalt het Tilestump object uit de server om hem vervolgens in de client te kunnen plaatsen
			stump = client.getTile();
			alleSpelers = RmiStub.getPlayerListObject();
			kaartenOver = RmiStub.getKaartenLeft();

		} catch (RemoteException e) {
			e.printStackTrace();
		}
		tileViews[stump.getX()][stump.getY()].setRotation(stump.getRotation());
		tileViews[stump.getX()][stump.getY()].setKaartId(stump.getId());
		if (stump.getGeplaatsteHorige() != null) {
			tileViews[stump.getX()][stump.getY()].plaatsHorige(stump.getGeplaatsteHorige());
			System.out.println("Horige is niet null!");
		}

		addTilePreviews(stump.getX(), stump.getY());
		System.out.println(stump.getX() + " " + stump.getY() + " " + stump.getRotation());
		Platform.runLater(() -> {
			KaartenLeft.setText("Kaarten over " + kaartenOver);
			for (int i = 0; i < playerViews.length; i++) {
				if (i == alleSpelers.size()) {
					return;
				}
				playerViews[i].setNaam(alleSpelers.get(i).getNaam());
				playerViews[i].setPunten(alleSpelers.get(i).getPunten());
			}
		});
	}

	public MenuController getController() {
		return controller;
	}

	public void setSceneBlur() {
		this.mainPane.setEffect(new GaussianBlur());
		tilesPane.setEffect(new GaussianBlur());
	}

	public void hideSceneBlur() {
		this.mainPane.setEffect(null);
		tilesPane.setEffect(null);
	}
}