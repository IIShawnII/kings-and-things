package client.gui.components;

import java.util.Collection;

public interface ISelectionListener<T>
{
	void selectionChanged(Collection<T> newSelection);
}
