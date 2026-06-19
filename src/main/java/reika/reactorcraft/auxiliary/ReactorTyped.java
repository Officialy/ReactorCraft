package reika.reactorcraft.auxiliary;

import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;

public interface ReactorTyped {

	public ReactorType getReactorType();

	public ReactorTiles getTile();

}
