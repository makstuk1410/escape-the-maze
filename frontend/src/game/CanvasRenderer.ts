import type { Direction } from "../types/game";
import type { TileType } from "../api";

export interface MazeViewport {
  tiles: TileType[][];
  playerX: number;
  playerY: number;
  size?: number;
}

export class CanvasRenderer {
  private readonly context: CanvasRenderingContext2D;

  constructor(private readonly canvas: HTMLCanvasElement) {
    const context = canvas.getContext("2d");

    if (!context) {
      throw new Error("Canvas 2D context is unavailable.");
    }

    this.context = context;
  }

  renderPlaceholder(lastDirection?: Direction): void {
    const { context, canvas } = this;
    const cellSize = 44;
    const columns = 14;
    const rows = 10;

    context.fillStyle = "#081A2B";
    context.fillRect(0, 0, canvas.width, canvas.height);

    context.fillStyle = "#397FC1";
    for (let row = 0; row < rows; row += 1) {
      for (let column = 0; column < columns; column += 1) {
        const isBorder = row === 0 || column === 0 || row === rows - 1 || column === columns - 1;
        const isWall = (row === 2 && column > 2 && column < 9) || (column === 5 && row > 3 && row < 8);

        if (isBorder || isWall) {
          context.fillRect(50 + column * cellSize, 38 + row * cellSize, cellSize - 5, cellSize - 5);
        }
      }
    }

    context.fillStyle = "#00A5DE";
    context.beginPath();
    context.arc(50 + 7 * cellSize + 20, 38 + 5 * cellSize + 20, 15, 0, Math.PI * 2);
    context.fill();

    context.fillStyle = "#FFCC00";
    context.fillRect(50 + 11 * cellSize + 10, 38 + 8 * cellSize + 10, 20, 20);

    context.fillStyle = "#A9BED1";
    context.font = "14px Inter, sans-serif";
    context.fillText(lastDirection ? `Last input: ${lastDirection}` : "Canvas renderer placeholder", 24, canvas.height - 24);
  }

  /** Draws the server-owned maze grid. Tile effects are added in later renderer tasks. */
  renderMaze(tiles: TileType[][]): void {
    const { canvas, context } = this;
    const rows = tiles.length;
    const columns = tiles[0]?.length ?? 0;

    context.fillStyle = "#081A2B";
    context.fillRect(0, 0, canvas.width, canvas.height);
    if (rows === 0 || columns === 0) return;

    const padding = Math.max(16, Math.round(Math.min(canvas.width, canvas.height) * 0.035));
    const cellSize = Math.min(
      (canvas.width - padding * 2) / columns,
      (canvas.height - padding * 2) / rows,
    );
    const gridWidth = cellSize * columns;
    const gridHeight = cellSize * rows;
    const originX = (canvas.width - gridWidth) / 2;
    const originY = (canvas.height - gridHeight) / 2;

    context.fillStyle = "#104243";
    context.fillRect(originX, originY, gridWidth, gridHeight);

    for (let row = 0; row < rows; row += 1) {
      for (let column = 0; column < columns; column += 1) {
        if (tiles[row][column] !== "WALL") continue;
        context.fillStyle = "#397FC1";
        context.fillRect(
          originX + column * cellSize + 1,
          originY + row * cellSize + 1,
          Math.max(0, cellSize - 2),
          Math.max(0, cellSize - 2),
        );
      }
    }
  }

  /**
   * Draws a camera window into the full maze (11 × 11 by default).
   * At an edge, the camera stops at the real maze boundary instead of inventing walls.
   */
  renderViewport({ tiles, playerX, playerY, size = 11 }: MazeViewport): void {
    if (size % 2 === 0) throw new Error("Maze viewport size must be odd.");

    const { canvas, context } = this;
    const padding = Math.max(16, Math.round(Math.min(canvas.width, canvas.height) * 0.035));
    const mazeRows = tiles.length;
    const mazeColumns = tiles[0]?.length ?? 0;
    if (mazeRows === 0 || mazeColumns === 0) return;

    const visibleColumns = Math.min(size, mazeColumns);
    const visibleRows = Math.min(size, mazeRows);
    const startColumn = clamp(playerX - Math.floor(size / 2), 0, mazeColumns - visibleColumns);
    const startRow = clamp(playerY - Math.floor(size / 2), 0, mazeRows - visibleRows);
    const cellSize = Math.min(
      (canvas.width - padding * 2) / size,
      (canvas.height - padding * 2) / size,
    );
    const gridWidth = cellSize * visibleColumns;
    const gridHeight = cellSize * visibleRows;
    const originX = (canvas.width - gridWidth) / 2;
    const originY = (canvas.height - gridHeight) / 2;

    context.fillStyle = "#081A2B";
    context.fillRect(0, 0, canvas.width, canvas.height);
    context.fillStyle = "#104243";
    context.fillRect(originX, originY, gridWidth, gridHeight);

    for (let viewportY = 0; viewportY < visibleRows; viewportY += 1) {
      for (let viewportX = 0; viewportX < visibleColumns; viewportX += 1) {
        const tile = tiles[startRow + viewportY][startColumn + viewportX];
        if (tile !== "WALL") continue;

        context.fillStyle = "#397FC1";
        context.fillRect(
          originX + viewportX * cellSize + 1,
          originY + viewportY * cellSize + 1,
          Math.max(0, cellSize - 2),
          Math.max(0, cellSize - 2),
        );
      }
    }

    // Near maze boundaries the player moves away from the visual centre, like the JavaFX camera.
    context.fillStyle = "#00A5DE";
    context.beginPath();
    context.arc(
      originX + (playerX - startColumn + 0.5) * cellSize,
      originY + (playerY - startRow + 0.5) * cellSize,
      Math.max(5, cellSize * 0.3),
      0,
      Math.PI * 2,
    );
    context.fill();
  }
}

function clamp(value: number, minimum: number, maximum: number): number {
  return Math.max(minimum, Math.min(maximum, value));
}
