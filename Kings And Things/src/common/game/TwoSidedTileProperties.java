package common.game;

import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.Category;
import common.Constants.Restriction;

//TODO Needs to be optimized
public class TwoSidedTileProperties extends TileProperties {
	
	private static final long serialVersionUID = -6715600368392464765L;
	
	@SuppressWarnings("unused")
	private final TileProperties faceUp;	// keeps track of the face up property of tile
	@SuppressWarnings("unused")
	private final TileProperties faceDown;  // keeps track of the face up property of tile
	
	public TwoSidedTileProperties (TileProperties faceUp, TileProperties faceDown) {
		this.faceUp = faceUp;
		this.faceDown = faceDown;
	}

	@Override
	// Ask Shahriar about this
	public boolean isFake() {
		// TODO Auto-generated method stub
		return super.isFake();
	}

	@Override
	public Category getCategory() {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			return faceUp.getCategory();
		} else {
			return faceDown.getCategory();
		}
	}

	@Override
	public int getNumber() {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			return faceUp.getNumber();
		} else {
			return faceDown.getNumber();
		}
	}

	@Override
	public int getValue() {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			return faceUp.getValue();
		} else {
			return faceDown.getValue();
		}
	}

	@Override
	public void setValue(int value) {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			faceUp.setValue(value);
		} else {
			faceDown.setValue(value);
		}
	}

	@Override
	public void resetValue() {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			faceUp.resetValue();
		} else {
			faceDown.resetValue();
		}
	}

	@Override
	public int getMoveSpeed() {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			return faceUp.getMoveSpeed();
		} else {
			return faceDown.getMoveSpeed();
		}
	}

	@Override
	public void setMoveSpeed(int moveSpeed) {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			faceUp.setMoveSpeed(moveSpeed);
		} else {
			faceDown.setMoveSpeed(moveSpeed);
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			return faceUp.getName();
		} else {
			return faceDown.getName();
		}
	}

	@Override
	public Ability[] getAbilities() {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			return faceUp.getAbilities();
		} else {
			return faceDown.getAbilities();
		}
	}

	@Override
	public boolean isFaceUp() {
		// TODO Auto-generated method stub
		return super.isFaceUp();
	}

	@Override
	public boolean isInfinit() {
		// TODO Auto-generated method stub
		return faceUp.isInfinit();
	}

	@Override
	public boolean isBuilding() {
		// TODO Auto-generated method stub
		return faceUp.isBuilding();
	}

	@Override
	public boolean isBuildableBuilding() {
		// TODO Auto-generated method stub
		return faceUp.isBuildableBuilding();
	}

	@Override
	public boolean isRestrictedToBiome() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCreature() {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			return faceUp.isCreature();
		} else {
			return faceDown.isCreature();
		}
	}

	@Override
	public boolean hasAbility(Ability ability) {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			return faceUp.hasAbility(ability);
		} else {
			return faceDown.hasAbility(ability);
		}
	}

	@Override
	public boolean isSpecialCreatureWithAbility(Ability ability) {
		// TODO Auto-generated method stub
		if (isFaceUp()) {
			return faceUp.isSpecialCreatureWithAbility(ability);
		} else {
			return faceDown.isSpecialCreatureWithAbility(ability);
		}
	}

	@Override
	public Biome getBiomeRestriction() {
		// TODO Auto-generated method stub
		return faceUp.getBiomeRestriction();
	}

	@Override
	public Restriction getRestriction(int index) {
		// TODO Auto-generated method stub
		return faceUp.getRestriction(index);
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
		if (isFaceUp()) {
			return faceUp.hasRestriction();
		} else {
			return faceDown.hasRestriction();
		}
	}
	
}
