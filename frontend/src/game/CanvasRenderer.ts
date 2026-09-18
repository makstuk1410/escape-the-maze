import type { Direction } from "../types/game";

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
}
