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
    const mazeRows = tiles.length;
    const mazeColumns = tiles[0]?.length ?? 0;
    if (mazeRows === 0 || mazeColumns === 0) return;

    const visibleColumns = Math.min(size, mazeColumns);
    const visibleRows = Math.min(size, mazeRows);
    const startColumn = clamp(playerX - Math.floor(size / 2), 0, mazeColumns - visibleColumns);
    const startRow = clamp(playerY - Math.floor(size / 2), 0, mazeRows - visibleRows);
    // Figma viewport: 660px maze floor in a 720px square, with 11 × 60px slots.
    const floorSize = Math.min(canvas.width, canvas.height) * (11 / 12);
    const cellSize = floorSize / size;
    const floorX = (canvas.width - floorSize) / 2;
    const floorY = (canvas.height - floorSize) / 2;
    const originX = floorX + cellSize * 0.05;
    const originY = floorY + cellSize * 0.05;

    context.fillStyle = "#081A2B";
    context.fillRect(0, 0, canvas.width, canvas.height);
    context.fillStyle = "#104243";
    this.roundedRect(floorX, floorY, floorSize, floorSize, cellSize * 0.08);
    context.fill();

    for (let viewportY = 0; viewportY < visibleRows; viewportY += 1) {
      for (let viewportX = 0; viewportX < visibleColumns; viewportX += 1) {
        const tile = tiles[startRow + viewportY][startColumn + viewportX];
        const x = originX + viewportX * cellSize;
        const y = originY + viewportY * cellSize;
        if (tile !== "WALL") {
          this.drawSpecialTile(tile, x, y, cellSize);
          continue;
        }

        this.drawWallTile(x, y, cellSize);
      }
    }

    // Near maze boundaries the player moves away from the visual centre, like the JavaFX camera.
    const playerCenterX = originX + (playerX - startColumn + 0.5) * cellSize;
    const playerCenterY = originY + (playerY - startRow + 0.5) * cellSize;
    const playerRadius = Math.max(5, cellSize * 0.28);
    const playerGlow = context.createRadialGradient(
      playerCenterX, playerCenterY, playerRadius * 0.25,
      playerCenterX, playerCenterY, playerRadius * 1.8,
    );
    playerGlow.addColorStop(0, "rgb(0 225 255 / 72%)");
    playerGlow.addColorStop(1, "rgb(0 165 222 / 0%)");
    context.fillStyle = playerGlow;
    context.beginPath();
    context.arc(playerCenterX, playerCenterY, playerRadius * 1.8, 0, Math.PI * 2);
    context.fill();
    context.fillStyle = "#00A5DE";
    context.beginPath();
    context.arc(
      playerCenterX,
      playerCenterY,
      playerRadius,
      0,
      Math.PI * 2,
    );
    context.fill();
    context.strokeStyle = "#D9FAFF";
    context.lineWidth = Math.max(1, cellSize * 0.04);
    context.stroke();
  }

  private drawWallTile(x: number, y: number, cellSize: number): void {
    const { context } = this;
    const inset = Math.max(1, cellSize * 0.05);
    const size = cellSize - inset * 2;
    context.fillStyle = "#397FC1";
    this.roundedRect(x + inset, y + inset, size, size, cellSize * 0.075);
    context.fill();
  }

  private drawSpecialTile(tile: TileType, x: number, y: number, cellSize: number): void {
    const { context } = this;
    const centerX = x + cellSize / 2;
    const centerY = y + cellSize / 2;

    if (tile === "GOLD" || tile === "EXIT") {
      const radius = cellSize * (tile === "GOLD" ? 0.28 : 0.25);
      const glow = context.createRadialGradient(centerX, centerY, radius * 0.2, centerX, centerY, radius * 1.75);
      glow.addColorStop(0, tile === "GOLD" ? "rgb(255 244 163 / 88%)" : "rgb(150 255 199 / 80%)");
      glow.addColorStop(1, tile === "GOLD" ? "rgb(255 204 0 / 0%)" : "rgb(88 214 141 / 0%)");
      context.fillStyle = glow;
      context.beginPath();
      context.arc(centerX, centerY, radius * 1.75, 0, Math.PI * 2);
      context.fill();
      context.fillStyle = tile === "GOLD" ? "#FFCC00" : "#58D68D";
      context.beginPath();
      context.arc(centerX, centerY, radius, 0, Math.PI * 2);
      context.fill();
      context.strokeStyle = tile === "GOLD" ? "#FFF0A3" : "#D8FFE5";
      context.lineWidth = Math.max(1, cellSize * 0.035);
      context.stroke();
      if (tile === "GOLD") {
        context.fillStyle = "#C98700";
        context.fillRect(centerX - radius * 0.12, centerY - radius * 0.56, radius * 0.24, radius * 1.12);
      }
      return;
    }

    if (tile === "SPIKES") {
      context.fillStyle = "#8B1E2D";
      const rows = 3;
      const columns = 3;
      for (let row = 0; row < rows; row += 1) {
        for (let column = 0; column < columns; column += 1) {
          const spikeX = x + (column + 0.5) * (cellSize / columns);
          const spikeY = y + (row + 0.62) * (cellSize / rows);
          const halfWidth = cellSize * 0.1;
          const height = cellSize * 0.18;
          context.beginPath();
          context.moveTo(spikeX, spikeY - height);
          context.lineTo(spikeX - halfWidth, spikeY + height * 0.45);
          context.lineTo(spikeX + halfWidth, spikeY + height * 0.45);
          context.closePath();
          context.fill();
        }
      }
      return;
    }

    if (tile === "FREEZE") {
      const ice = context.createLinearGradient(x, y, x + cellSize, y + cellSize);
      ice.addColorStop(0, "#E7FBFF");
      ice.addColorStop(1, "#8EDBE8");
      context.fillStyle = ice;
      this.roundedRect(x + 2, y + 2, cellSize - 4, cellSize - 4, cellSize * 0.08);
      context.fill();
      context.strokeStyle = "#00ACC1";
      context.lineWidth = Math.max(1, cellSize * 0.045);
      const length = cellSize * 0.23;
      context.beginPath();
      context.moveTo(centerX, centerY - length);
      context.lineTo(centerX, centerY + length);
      context.moveTo(centerX - length, centerY);
      context.lineTo(centerX + length, centerY);
      context.moveTo(centerX - length, centerY - length);
      context.lineTo(centerX + length, centerY + length);
      context.moveTo(centerX - length, centerY + length);
      context.lineTo(centerX + length, centerY - length);
      context.stroke();
      return;
    }

    if (tile === "FOG") {
      const fog = context.createRadialGradient(centerX, centerY, cellSize * 0.08, centerX, centerY, cellSize * 0.62);
      fog.addColorStop(0, "#9BA8AD");
      fog.addColorStop(0.55, "#66767D");
      fog.addColorStop(1, "#35454D");
      context.fillStyle = fog;
      this.roundedRect(x + 2, y + 2, cellSize - 4, cellSize - 4, cellSize * 0.08);
      context.fill();
    }
  }

  private roundedRect(x: number, y: number, width: number, height: number, radius: number): void {
    this.context.beginPath();
    this.context.roundRect(x, y, width, height, radius);
  }
}

function clamp(value: number, minimum: number, maximum: number): number {
  return Math.max(minimum, Math.min(maximum, value));
}
