package common.game;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

import static common.Constants.RESOURCE_PATH;
import static common.Constants.LOAD_BUILDING;
import static common.Constants.LOAD_RESOURCE;
import static common.Constants.LOAD_SPECIAL;
import static common.Constants.LOAD_STATE;
import static common.Constants.LOAD_GOLD;
import static common.Constants.PROGRESS;
import static common.Constants.LOAD_CUP;
import static common.Constants.LOAD_HEX;
import static common.Constants.BUILDING;
import static common.Constants.SPECIAL;
import static common.Constants.IMAGES;
import static common.Constants.STATE;
import static common.Constants.GOLD;
import static common.Constants.CUP;
import static common.Constants.HEX;
import common.Constants;
import common.Constants.Biome;
import common.Constants.Ability;
import common.Constants.Building;
import common.Constants.Category;
import common.Constants.UpdateKey;
import common.Constants.Restriction;
import common.Constants.UpdateInstruction;
import common.event.AbstractUpdateReceiver;
import common.event.UpdatePackage;

public class LoadResources implements Runnable, FileVisitor< Path>{

	private int copyTile = 0;
	private boolean loadImages;
	private Category currentCategory = null;
	private Category currentCupCategory = null;
	private final Path RESOURCES_DIRECTORY;
	private UpdatePackage update = null;
	private FileVisitResult result = FileVisitResult.CONTINUE;
	
	public LoadResources( boolean loadImages){
		this( RESOURCE_PATH, loadImages);
	}
	
	public LoadResources(String directory, boolean loadImages){
		RESOURCES_DIRECTORY = Paths.get(directory);
		this.loadImages = loadImages;
		update = new UpdatePackage("LoadResources");
		new UpdateReceiver();
	}
	
	@Override
	public void run() {
		try {
			update.addInstruction( UpdateInstruction.Category);
			Files.walkFileTree( RESOURCES_DIRECTORY, this);
			update.clearDate();
			update.putData( UpdateKey.Command, Category.END);
			update.postCommand( PROGRESS);
		} catch ( IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs) throws IOException {
		if( !(currentCategory!=null && currentCategory==Category.Cup && dir.toString().contains( Category.Cup.name()))){
			try{	
				currentCategory = Category.valueOf( dir.getFileName().toString());
			}catch( IllegalArgumentException e){
				currentCategory = null;
			}
		}else{
			try{
				Biome.valueOf( dir.getFileName().toString());
				currentCupCategory = Category.Creature;
			}catch( IllegalArgumentException e){
				currentCupCategory = Category.valueOf( dir.getFileName().toString());
			}
		}
		return result;
	}

	@Override
	public FileVisitResult visitFile( Path file, BasicFileAttributes attrs) throws IOException {
		update.clearDate();
		if( currentCategory!=null && currentCategory!=Category.Resources  && currentCategory!=Category.Misc){
			TileProperties tile = createTile( file.getFileName().toString());
			switch( currentCategory){
				case Building:
					if( tile.getName().equals( Building.City) || tile.getName().equals( Building.Village)){
						tile.setCategory( Category.Building);
						for(int i=0; i<6; i++){
							TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
							CUP.put( tileCopy.hashCode(), tileCopy);
							if( loadImages || LOAD_BUILDING){
								IMAGES.put( tileCopy.hashCode(), ImageIO.read( file.toFile()));
							}
						}
					}else{
						tile.setInfinite();
						tile.setSpecialFlip();
						tile.setCategory( Category.Buildable);
						BUILDING.put( tile.hashCode(), tile);
						if( loadImages || LOAD_BUILDING){
							IMAGES.put( tile.hashCode(), ImageIO.read( file.toFile()));
						}
					}
					break;
				case Cup:
					switch( currentCupCategory){
						case Event:
						case Magic:
						case Treasure:
							tile.setCategory( currentCupCategory);
							break;
						default:
							tile.setCategory( Category.Creature);
							tile.setMoveSpeed( Constants.MAX_MOVE_SPEED);
					}
					tile.setCategory( currentCupCategory);
					if( copyTile==0){
						CUP.put( tile.hashCode(), tile);
						if( loadImages || LOAD_CUP){
							IMAGES.put( tile.hashCode(), ImageIO.read( file.toFile()));
						}
					}else{
						for( int i=0; i<copyTile; i++){
							TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
							CUP.put( tileCopy.hashCode(), tileCopy);
							if( loadImages || LOAD_CUP){
								IMAGES.put( tileCopy.hashCode(), ImageIO.read( file.toFile()));
							}
						}
					}
					break;
				case Gold:
					tile.setNoFlip();
					tile.setInfinite();
					tile.setCategory( currentCategory);
					GOLD.put( tile.hashCode(), tile);
					if( loadImages || LOAD_GOLD){
						IMAGES.put( tile.hashCode(), ImageIO.read( file.toFile()));
					}
					break;
				case Hex:
					switch(tile.getName()){
						case "Swamp":
						case "Mountain":
						case "Forest":
						case "Jungle":
							tile.setMoveSpeed(2);
							break;
						default:
							tile.setMoveSpeed(1);
					}
					tile.setCategory( currentCategory);
					for( int i=0; i<copyTile; i++){
						TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
						HEX.put( tileCopy.hashCode(), tileCopy);
						if( loadImages || LOAD_HEX){
							IMAGES.put( tileCopy.hashCode(), ImageIO.read( file.toFile()));
						}
					}
					break;
				case Special:
					tile.setMoveSpeed(Constants.MAX_MOVE_SPEED);
					tile.setSpecialFlip();
					SPECIAL.put( tile.hashCode(), tile);
					tile.setCategory( currentCategory);
					if( loadImages || LOAD_SPECIAL){
						IMAGES.put( tile.hashCode(), ImageIO.read( file.toFile()));
					}
					break;
				case State:
					tile.setNoFlip();
					tile.setInfinite();
					tile.setCategory( currentCategory);
					STATE.put( tile.getRestriction( 0), tile);
					if( loadImages || LOAD_STATE){
						IMAGES.put( tile.hashCode(), ImageIO.read( file.toFile()));
					}
					break;
					
				case Resources:
				default:
					//will never be called
			}
			copyTile = 0;
		}
		update.addInstruction( UpdateInstruction.Category);
		update.putData( UpdateKey.Command, currentCategory);
		update.postCommand( PROGRESS);
		return result;
	}
	
	private TileProperties createTile( String name){
		String[] array = name.substring( 0, name.lastIndexOf( ".")).split( " ");
		TileProperties tile = new TileProperties();
		for( int i=0; i<array.length-1; i++){
			switch( array[i]){
				case "-n": tile.setName( array[++i]);break;
				case "-t": tile.addRestriction( Restriction.valueOf( array[++i]));break;
				case "-c": copyTile = Integer.parseInt( array[++i]);break;
				case "-s": tile.addAbilities( Ability.valueOf( array[++i]));break;
				case "-a": tile.setValue( Integer.parseInt( array[++i]));break;
				default: 
					throw new IllegalArgumentException("ERROR - incorrect file name \"" + name + "\n");
			}
		}
		return tile;
	}

	@Override
	public FileVisitResult visitFileFailed( Path file, IOException exc) throws IOException {
		return result;
	}

	@Override
	public FileVisitResult postVisitDirectory( Path dir, IOException exc) throws IOException {
		return result;
	}
	
	private class UpdateReceiver extends AbstractUpdateReceiver<UpdatePackage>{

		protected UpdateReceiver() {
			super( INTERNAL, LOAD_RESOURCE);
		}

		@Override
		public void handle( UpdatePackage update) {
			if( update.hasInstructions() && update.getInstructions()[0]==UpdateInstruction.End){
				result = FileVisitResult.TERMINATE;
			}
		}

		@Override
		public boolean verify( UpdatePackage update) {
			return (!update.isPublic() && (update.getID()&ID)==ID);
		}
	}
}
