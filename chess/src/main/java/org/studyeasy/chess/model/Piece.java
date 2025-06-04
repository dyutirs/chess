package org.studyeasy.chess.model;

public abstract class Piece {
    private final PieceColor color;
    private final PieceType type;
    private boolean hasMoved = false;
    
    public Piece(PieceColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }
    
    public PieceColor getColor() {
        return color;
    }
    
    public PieceType getType() {
        return type;
    }
    
    public boolean hasMoved() {
        return hasMoved;
    }
    
    public void setMoved() {
        this.hasMoved = true;
    }
    
    public abstract boolean isValidMove(Board board, Position from, Position to);
    
    // Helper method to check if a position is occupied by a piece of the same color
    protected boolean isSameColorPiece(Board board, Position position) {
        Piece piece = board.getPiece(position);
        return piece != null && piece.getColor() == this.color;
    }
    
    // Helper method to check if a path is clear (for pieces that move in straight lines)
    protected boolean isPathClear(Board board, Position from, Position to) {
        int rowStep = Integer.compare(to.row(), from.row());
        int colStep = Integer.compare(to.col(), from.col());
        
        int row = from.row() + rowStep;
        int col = from.col() + colStep;
        
        while (row != to.row() || col != to.col()) {
            if (board.getPiece(new Position(row, col)) != null) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }
        
        return true;
    }
}
