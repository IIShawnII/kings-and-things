package server.logic.game;

import common.game.Roll;

public class RollModification
{
	private final Roll rollToModify;
	private final int amountToAdd;
	private final int rollIndexToModify;
	
	public RollModification(Roll rollToModify, int amountToAdd, int rollIndexToModify)
	{
		this.rollToModify = rollToModify;
		this.amountToAdd = amountToAdd;
		this.rollIndexToModify = rollIndexToModify;
	}

	public Roll getRollToModify()
	{
		return rollToModify;
	}

	public int getAmountToAdd()
	{
		return amountToAdd;
	}
	
	public int getRollIndexToModify()
	{
		return rollIndexToModify;
	}
}
