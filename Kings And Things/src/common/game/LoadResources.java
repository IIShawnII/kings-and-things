package common.game;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

import common.Constants;
import common.Constants.Biome;
import common.Constants.Ability;
import common.Constants.Building;
import common.Constants.Category;
import common.Constants.UpdateKey;
import common.Constants.Restriction;
import common.Constants.UpdateInstruction;

import common.event.UpdatePackage;
import common.event.AbstractUpdateReceiver;

public class LoadResources implements Runnable, FileVisitor< Path>{

	private int copyTile = 0;
	private boolean isClient;
	private Category currentCategory = null;
	private Category currentCupCategory = null;
	private final Path RESOURCES_DIRECTORY;
	private UpdatePackage update = null;
	private FileVisitResult result = FileVisitResult.CONTINUE;
	private UpdateReceiver receiver;
	
	public LoadResources( boolean isServer){
		this( Constants.RESOURCE_PATH, isServer);
	}
	
	public LoadResources( String directory, boolean isServer){
		RESOURCES_DIRECTORY = Paths.get(directory);
		this.isClient = isServer;
		update = new UpdatePackage("LoadResources", this);
	}
	
	@Override
	public void run() {
		receiver = new UpdateReceiver();
		try {
			update.addInstruction( UpdateInstruction.Category);
			Files.walkFileTree( RESOURCES_DIRECTORY, this);
			update.clearData();
			update.putData( UpdateKey.Category, Category.END);
			update.postInternalEvent( Constants.PROGRESS);
		} catch ( IOException e) {
			e.printStackTrace();
		}
		receiver.unregisterFromEventBus();
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
		update.clearData();
		if( currentCategory!=null && currentCategory!=Category.Resources  && currentCategory!=Category.Misc){
			TileProperties tile = createTile( file.getFileName().toString());
			switch( currentCategory){
				case Building:
					if( tile.getName().equals( Building.City.name()) || tile.getName().equals( Building.Village.name())){
						tile.setCategory( Category.Building);
						for(int i=0; i<6; i++){
							TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
							Constants.BUILDING.put( tileCopy.hashCode(), tileCopy);
							//Constants.CUP.put( tileCopy.hashCode(), tileCopy);
							addImage(tileCopy.hashCode(), file);
						}
					}else{
						tile.setInfinite();
						tile.setSpecialFlip();
						tile.setCategory( Category.Buildable);
						Constants.BUILDING.put( tile.hashCode(), tile);
						addImage(tile.hashCode(), file);
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
						Constants.CUP.put( tile.hashCode(), tile);
						addImage(tile.hashCode(), file);
					}else{
						for( int i=0; i<copyTile; i++){
							TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
							Constants.CUP.put( tileCopy.hashCode(), tileCopy);
							addImage(tileCopy.hashCode(), file);
						}
					}
					break;
				case Gold:
					tile.setNoFlip();
					tile.setInfinite();
					tile.setCategory( currentCategory);
					Constants.GOLD.put( tile.hashCode(), tile);
					addImage(tile.hashCode(), file);
					break;
				case Hex:
					if(tile.getName().equals("Swamp") || tile.getName().equals("Mountain") || tile.getName().equals("Forest") || tile.getName().equals("Jungle")){
						tile.setMoveSpeed(2);
					}else{
						tile.setMoveSpeed(1);
					}
					/*switch(tile.getName()){
						case "Swamp":
						case "Mountain":
						case "Forest":
						case "Jungle":
							tile.setMoveSpeed(2);
							break;
						default:
							tile.setMoveSpeed(1);
					}*/
					tile.setCategory( currentCategory);
					for( int i=0; i<copyTile; i++){
						TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
						Constants.HEX.put( tileCopy.hashCode(), tileCopy);
						addImage(tileCopy.hashCode(), file);
					}
					break;
				case Special:
					tile.setMoveSpeed(Constants.MAX_MOVE_SPEED);
					tile.setSpecialFlip();
					Constants.SPECIAL.put( tile.hashCode(), tile);
					tile.setCategory( currentCategory);
					addImage(tile.hashCode(), file);
					break;
				case State:
					tile.setNoFlip();
					tile.setInfinite();
					tile.setCategory( currentCategory);
					Constants.STATE.put( tile.getRestriction( 0), tile);
					if( isClient){
						Constants.IMAGE_MARKERS.put( tile.getRestriction( 0), ImageIO.read( file.toFile()));
					}
					addImage( tile.hashCode(), file);
					break;
					
				case Resources:
				default:
					//will never be called
			}
			copyTile = 0;
			update.putData( UpdateKey.Category, currentCategory);
			update.postInternalEvent( Constants.PROGRESS);
		}
		return result;
	}
	
	private void addImage( final int hashCode, Path file) throws IOException{
		if( isClient){
			Constants.IMAGES.put( hashCode, ImageIO.read( file.toFile()));
		}
	}
	
	private TileProperties createTile( String name){
		String[] array = name.substring( 0, name.lastIndexOf( ".")).split( " ");
		TileProperties tile = new TileProperties();
		for( int i=0; i<array.length-1; i++){
			if(array[i].equals("-n")){
				tile.setName( array[++i]);
			}else if(array[i].equals("-t")){
				tile.addRestriction( Restriction.valueOf( array[++i]));
			}else if(array[i].equals("-c")){
				copyTile = Integer.parseInt( array[++i]);
			}else if(array[i].equals("-s")){
				tile.addAbilities( Ability.valueOf( array[++i]));
			}else if(array[i].equals("-a")){
				tile.setValue( Integer.parseInt( array[++i]));
				tile.setBaseValue(tile.getValue());
			}else{
				throw new IllegalArgumentException("ERROR - incorrect file name \"" + name + "\n");
			}
			/*switch( array[i]){
				case "-n": tile.setName( array[++i]);break;
				case "-t": tile.addRestriction( Restriction.valueOf( array[++i]));break;
				case "-c": copyTile = Integer.parseInt( array[++i]);break;
				case "-s": tile.addAbilities( Ability.valueOf( array[++i]));break;
				case "-a": tile.setValue( Integer.parseInt( array[++i])); tile.setBaseValue(tile.getValue());break;
				default: 
					throw new IllegalArgumentException("ERROR - incorrect file name \"" + name + "\n");
			}*/
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
			super( INTERNAL, Constants.LOAD_RESOURCE, LoadResources.this);
		}

		@Override
		protected void handlePublic( UpdatePackage update) {
			if( update.hasInstructions() && update.peekFirstInstruction()==UpdateInstruction.End){
				result = FileVisitResult.TERMINATE;
			}
		}
	}
}
