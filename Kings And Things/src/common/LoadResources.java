package common;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

import common.Constants.Ability;
import common.Constants.Category;
import common.Constants.Level;
import common.Constants.Restriction;
import common.event.EventMonitor;
import static common.Constants.CUP;
import static common.Constants.HEX;
import static common.Constants.GOLD;
import static common.Constants.STATE;
import static common.Constants.UPDATE;
import static common.Constants.SPECIAL;
import static common.Constants.BUILDING;
import static common.Constants.RESOURCE_PATH;

public class LoadResources implements Runnable, FileVisitor< Path>{

	private int copyTile = 0;
	private Category currentCategory = null;
	
	@Override
	public void run() {
		try {
			Thread.sleep( 400);
		} catch ( InterruptedException e) {}
		try {
			Files.walkFileTree( RESOURCE_PATH, this);
		} catch ( IOException e) {
			e.printStackTrace();
		}
		EventMonitor.fireEvent( UPDATE, currentCategory, Level.END);
	}

	@Override
	public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs) throws IOException {
		try{
			if( !(currentCategory!=null && currentCategory==Category.Cup && dir.toString().contains( Category.Cup.name()))){
				currentCategory = Category.valueOf( dir.getFileName().toString());
			}
		}catch( IllegalArgumentException e){
			currentCategory = null;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile( Path file, BasicFileAttributes attrs) throws IOException {
		if( currentCategory!=null && currentCategory!=Category.Resources){
			TileProperties tile = createTile( file.getFileName().toString());
			switch( currentCategory){
				case Building:
					tile.setInfinite();
					tile.setSpecialFlip();
					BUILDING.put( tile.hashCode(), tile);
					break;
				case Cup:
					if( copyTile==0){
						CUP.put( tile.hashCode(), tile);
					}else{
						for( int i=0; i<copyTile; i++){
							TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
							CUP.put( tileCopy.hashCode(), tileCopy);
						}
					}
					break;
				case Gold:
					tile.setNoFlip();
					tile.setInfinite();
					GOLD.put( tile.hashCode(), tile);
					break;
				case Hex:
					for( int i=0; i<copyTile; i++){
						TileProperties tileCopy = new TileProperties( tile, tile.getNumber()+i);
						HEX.put( tileCopy.hashCode(), tileCopy);
					}
					break;
				case Special:
					tile.setSpecialFlip();
					SPECIAL.put( tile.hashCode(), tile);
					break;
				case State:
					tile.setNoFlip();
					tile.setInfinite();
					STATE.put( tile.getRestriction()[0], tile);
					break;
					
				case Resources:
				default:
					//will never be called
			}
			EventMonitor.fireEvent( UPDATE, currentCategory, Level.LOADING_DIALOG);
			copyTile = 0;
		}
		return FileVisitResult.CONTINUE;
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
				case "-a": tile.setAttack( Integer.parseInt( array[++i]));break;
				default: 
					throw new IllegalArgumentException("ERROR - incorrect file name \"" + name + "\n");
			}
		}
		return tile;
	}

	@Override
	public FileVisitResult visitFileFailed( Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory( Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}
}
