package common.game;

import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.Category;
import common.Constants.Restriction;

//TODO Needs to be optimized
public class TwoSidedTileProperties extends TileProperties {
	
	private final TileProperties faceUp;	// keeps track of the face up property of tile
	private final TileProperties faceDown;  // keeps track of the face up property of tile
	
	public TwoSidedTileProperties (TileProperties faceUp, TileProperties faceDown) {
		this.faceUp = faceUp;
		this.faceDown = faceDown;
	}

	@Override
	public boolean isFake() {
		// TODO Auto-generated method stub
		return super.isFake();
	}

	@Override
	public Category getCategory() {
		// TODO Auto-generated method stub
		return super.getCategory();
	}

	@Override
	public int getNumber() {
		// TODO Auto-generated method stub
		return super.getNumber();
	}

	@Override
	public int getValue() {
		// TODO Auto-generated method stub
		return super.getValue();
	}

	@Override
	public void setValue(int value) {
		// TODO Auto-generated method stub
		super.setValue(value);
	}

	@Override
	public void resetValue() {
		// TODO Auto-generated method stub
		super.resetValue();
	}

	@Override
	public int getMoveSpeed() {
		// TODO Auto-generated method stub
		return super.getMoveSpeed();
	}

	@Override
	public void setMoveSpeed(int moveSpeed) {
		// TODO Auto-generated method stub
		super.setMoveSpeed(moveSpeed);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}

	@Override
	public Ability[] getAbilities() {
		// TODO Auto-generated method stub
		return super.getAbilities();
	}

	@Override
	public boolean isFaceUp() {
		// TODO Auto-generated method stub
		return super.isFaceUp();
	}

	@Override
	public boolean isInfinit() {
		// TODO Auto-generated method stub
		return super.isInfinit();
	}

	@Override
	public boolean isBuilding() {
		// TODO Auto-generated method stub
		return super.isBuilding();
	}

	@Override
	public boolean isBuildableBuilding() {
		// TODO Auto-generated method stub
		return super.isBuildableBuilding();
	}

	@Override
	public boolean isRestrictedToBiome() {
		// TODO Auto-generated method stub
		return super.isRestrictedToBiome();
	}

	@Override
	public boolean isCreature() {
		// TODO Auto-generated method stub
		return super.isCreature();
	}

	@Override
	public boolean hasAbility(Ability ability) {
		// TODO Auto-generated method stub
		return super.hasAbility(ability);
	}

	@Override
	public boolean isSpecialCreatureWithAbility(Ability ability) {
		// TODO Auto-generated method stub
		return super.isSpecialCreatureWithAbility(ability);
	}

	@Override
	public Biome getBiomeRestriction() {
		// TODO Auto-generated method stub
		return super.getBiomeRestriction();
	}

	@Override
	public Restriction getRestriction(int index) {
		// TODO Auto-generated method stub
		return super.getRestriction(index);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	@Override
	public boolean hasRestriction() {
		// TODO Auto-generated method stub
		return super.hasRestriction();
	}
	
}
