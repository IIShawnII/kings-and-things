package common;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

import common.Constants.Category;

import static common.Constants.RESOURCE_PATH;

public class LoadResources implements Runnable, FileVisitor< Path>{

	private Category currentCategory = null;
	
	@Override
	public void run() {
		try {
			Files.walkFileTree( RESOURCE_PATH, this);
		} catch ( IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main( String[] args){
		new Thread( new LoadResources(), "Load Resources").start();
	}

	@Override
	public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs) throws IOException {
		try{
			currentCategory = Category.valueOf( dir.getFileName().toString());
		}catch( IllegalArgumentException e){
			currentCategory = null;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile( Path file, BasicFileAttributes attrs) throws IOException {
		if( currentCategory!=null){
			System.out.print( "Visit\t" + currentCategory + "\t\t");
			createTile( file.getFileName().toString());
			switch( currentCategory){
				case Building:
					break;
				case Cup:
					break;
				case Extra:
					break;
				case Gold:
					break;
				case Hex:
					break;
				case Special:
					break;
				case State:
					break;
				default:
					break;
			}
		}
		return FileVisitResult.CONTINUE;
	}
	
	private TileProperties createTile( String name){
		String[] array = name.substring( 0, name.lastIndexOf( ".")).split( " ");
		TileProperties tile = new TileProperties();
		for( String token : array){
			switch( token){
				case "-n": System.out.print( token);break;
				case "-t": System.out.print( token);break;
				case "-c": System.out.print( token);break;
				case "-s": System.out.print( token);break;
				case "-a": System.out.print( token);break;
				default: System.out.print( " " + token + " ");
			}
		}
		return tile;
	}

	@Override
	public FileVisitResult visitFileFailed( Path file, IOException exc) throws IOException {
		if( exc!=null){
			throw exc;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory( Path dir, IOException exc) throws IOException {
		if( exc!=null){
			throw exc;
		}
		return FileVisitResult.CONTINUE;
	}
}
