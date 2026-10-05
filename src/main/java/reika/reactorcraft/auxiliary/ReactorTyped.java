package reika.reactorcraft.auxiliary;

import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;

public interface ReactorTyped {

	ReactorType getReactorType();

	ReactorTiles getTile();

}
