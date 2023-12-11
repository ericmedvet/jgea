package io.github.ericmedvet.jgea.core.solver.cabea;

import io.github.ericmedvet.jsdynsym.grid.Grid;

public class MakeSubstrateGrid {
  private Grid<Boolean> grid;

  public MakeSubstrateGrid(String command, int gridSize) {
    grid = Grid.create(gridSize, gridSize, true); // Initialize the grid with true values

    if (command.equalsIgnoreCase("contour")) {
      setContourToFalse(gridSize);
    }
  }

  private void setContourToFalse(int gridSize) {
    // Setting the top and bottom rows
    for (int x = 0; x < gridSize; x++) {
      grid.set(new Grid.Key(x, 0), false); // Top row
      grid.set(new Grid.Key(x, gridSize - 1), false); // Bottom row
    }

    // Setting the left and right columns
    for (int y = 1; y < gridSize - 1; y++) {
      grid.set(new Grid.Key(0, y), false); // Left column
      grid.set(new Grid.Key(gridSize - 1, y), false); // Right column
    }
  }

  public Grid<Boolean> getGrid() {
    return grid;
  }
  // Other methods
}
