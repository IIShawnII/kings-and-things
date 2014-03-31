package server.logic.game;

import java.io.Serializable;

import common.game.Roll;

public class RollModification implements Serializable
{
	private static final long serialVersionUID = 1082522370317784392L;
	
	private final Roll rollToModify;
	private final int amountToAdd;
	private final int rollIndexToModify;
	
	public RollModification(Roll rollToModify, int amountToAdd, int rollIndexToModify)
	{
		this.rollToModify = rollToModify;
		this.amountToAdd = amountToAdd;
		this.rollIndexToModify = rollIndexToModify;
	}
	
	public RollModification(RollModification other)
	{
		rollToModify = other.rollToModify.clone();
		amountToAdd = other.amountToAdd;
		rollIndexToModify = other.rollIndexToModify;
	}
	
	@Override
	public RollModification clone()
	{
		return new RollModification(this);
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
