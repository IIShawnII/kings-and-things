package common.game;

import java.io.Serializable;

import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.Category;
import common.Constants.Restriction;

public interface ITileProperties extends Serializable
{
	Category getCategory();
	int getNumber();
	int getValue();
	void setValue( int value);
	void resetValue();
	// retrieves moveSpeed
	int getMoveSpeed ();
	// assigns new moveSpeed
	void setMoveSpeed(int moveSpeed);
	String getName();
	Ability[] getAbilities();
	void flip();
	boolean isFaceUp();
	boolean hasFlip();
	boolean isInfinit();
	boolean isHexTile();
	boolean isBuilding();
	boolean isBuildableBuilding();
	boolean isRestrictedToBiome();
	boolean isEvent();
	boolean isMagicItem();
	boolean isTreasure();
	boolean isCreature();
	boolean isSpecialCharacter();
	boolean hasAbility(Ability ability);
	boolean isSpecialCreatureWithAbility(Ability ability);
	Biome getBiomeRestriction();
	boolean isSpecialIncomeCounter();
	Restriction getRestriction( int index);
	boolean hasRestriction();
	ITileProperties clone();
	//used only on the client side
	boolean isFake();
}
