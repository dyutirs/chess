package org.studyeasy.chess.model;

public class Board {
    private final Piece[][] squares = new Piece[8][8];
    
    public Board() {
        initializeBoard();
    }
    
    private void initializeBoard() {
        // Set up initial chess position
        setupPawns();
        setupRooks();
        setupKnights();
        setupBishops();
        setupQueens();
        setupKings();
    }
    
    private void setupPawns() {
        // Setup pawns for both sides
        for (int col = 0; col < 8; col++) {
            squares[1][col] = createPiece(PieceType.PAWN, PieceColor.BLACK);
            squares[6][col] = createPiece(PieceType.PAWN, PieceColor.WHITE);
        }
    }
    
    private void setupRooks() {
        // Setup rooks for both sides
        squares[0][0] = createPiece(PieceType.ROOK, PieceColor.BLACK);
        squares[0][7] = createPiece(PieceType.ROOK, PieceColor.BLACK);
        squares[7][0] = createPiece(PieceType.ROOK, PieceColor.WHITE);
        squares[7][7] = createPiece(PieceType.ROOK, PieceColor.WHITE);
    }
    
    private void setupKnights() {
        // Setup knights for both sides
        squares[0][1] = createPiece(PieceType.KNIGHT, PieceColor.BLACK);
        squares[0][6] = createPiece(PieceType.KNIGHT, PieceColor.BLACK);
        squares[7][1] = createPiece(PieceType.KNIGHT, PieceColor.WHITE);
        squares[7][6] = createPiece(PieceType.KNIGHT, PieceColor.WHITE);
    }
    
    private void setupBishops() {
        // Setup bishops for both sides
        squares[0][2] = createPiece(PieceType.BISHOP, PieceColor.BLACK);
        squares[0][5] = createPiece(PieceType.BISHOP, PieceColor.BLACK);
        squares[7][2] = createPiece(PieceType.BISHOP, PieceColor.WHITE);
        squares[7][5] = createPiece(PieceType.BISHOP, PieceColor.WHITE);
    }
    
    private void setupQueens() {
        // Setup queens for both sides
        squares[0][3] = createPiece(PieceType.QUEEN, PieceColor.BLACK);
        squares[7][3] = createPiece(PieceType.QUEEN, PieceColor.WHITE);
    }
    
    private void setupKings() {
        // Setup kings for both sides
        squares[0][4] = createPiece(PieceType.KING, PieceColor.BLACK);
        squares[7][4] = createPiece(PieceType.KING, PieceColor.WHITE);
    }
    
    private Piece createPiece(PieceType type, PieceColor color) {
        return switch (type) {
            case PAWN -> new Piece(color, type) {
                @Override
                public boolean isValidMove(Board board, Position from, Position to) {
                    // Can't capture a piece of the same color
                    if (isSameColorPiece(board, to)) {
                        return false;
                    }
                    
                    int direction = (color == PieceColor.WHITE) ? -1 : 1;
                    int rowDiff = to.row() - from.row();
                    int colDiff = to.col() - from.col();
                    
                    // Forward movement (no capture)
                    if (colDiff == 0) {
                        // Single square forward
                        if (rowDiff == direction && board.getPiece(to) == null) {
                            return true;
                        }
                        
                        // Double square forward from starting position
                        boolean isStartingRow = (color == PieceColor.WHITE && from.row() == 6) || 
                                               (color == PieceColor.BLACK && from.row() == 1);
                        if (isStartingRow && rowDiff == 2 * direction && 
                            board.getPiece(to) == null && 
                            board.getPiece(new Position(from.row() + direction, from.col())) == null) {
                            return true;
                        }
                    }
                    
                    // Diagonal capture
                    if (Math.abs(colDiff) == 1 && rowDiff == direction && board.getPiece(to) != null) {
                        return true;
                    }
                    
                    return false;
                }
            };
            case ROOK -> new Piece(color, type) {
                @Override
                public boolean isValidMove(Board board, Position from, Position to) {
                    // Can't capture a piece of the same color
                    if (isSameColorPiece(board, to)) {
                        return false;
                    }
                    
                    // Rook moves horizontally or vertically
                    if (from.row() == to.row() || from.col() == to.col()) {
                        return isPathClear(board, from, to);
                    }
                    
                    return false;
                }
            };
            case KNIGHT -> new Piece(color, type) {
                @Override
                public boolean isValidMove(Board board, Position from, Position to) {
                    // Can't capture a piece of the same color
                    if (isSameColorPiece(board, to)) {
                        return false;
                    }
                    
                    // Knight moves in an L-shape: 2 squares in one direction and 1 square perpendicular
                    int rowDiff = Math.abs(to.row() - from.row());
                    int colDiff = Math.abs(to.col() - from.col());
                    
                    return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
                }
            };
            case BISHOP -> new Piece(color, type) {
                @Override
                public boolean isValidMove(Board board, Position from, Position to) {
                    // Can't capture a piece of the same color
                    if (isSameColorPiece(board, to)) {
                        return false;
                    }
                    
                    // Bishop moves diagonally
                    int rowDiff = Math.abs(to.row() - from.row());
                    int colDiff = Math.abs(to.col() - from.col());
                    
                    if (rowDiff == colDiff) {
                        return isPathClear(board, from, to);
                    }
                    
                    return false;
                }
            };
            case QUEEN -> new Piece(color, type) {
                @Override
                public boolean isValidMove(Board board, Position from, Position to) {
                    // Can't capture a piece of the same color
                    if (isSameColorPiece(board, to)) {
                        return false;
                    }
                    
                    // Queen moves like a rook or bishop
                    int rowDiff = Math.abs(to.row() - from.row());
                    int colDiff = Math.abs(to.col() - from.col());
                    
                    // Horizontal or vertical movement (like a rook)
                    if (from.row() == to.row() || from.col() == to.col()) {
                        return isPathClear(board, from, to);
                    }
                    
                    // Diagonal movement (like a bishop)
                    if (rowDiff == colDiff) {
                        return isPathClear(board, from, to);
                    }
                    
                    return false;
                }
            };
            case KING -> new Piece(color, type) {
                @Override
                public boolean isValidMove(Board board, Position from, Position to) {
                    // Can't capture a piece of the same color
                    if (isSameColorPiece(board, to)) {
                        return false;
                    }
                    
                    int rowDiff = Math.abs(to.row() - from.row());
                    int colDiff = Math.abs(to.col() - from.col());
                    
                    // Normal king move - one square in any direction
                    if (rowDiff <= 1 && colDiff <= 1) {
                        return true;
                    }
                    
                    // Check for castling
                    if (rowDiff == 0 && colDiff == 2 && !hasMoved()) {
                        // Determine if it's kingside or queenside castling
                        boolean isKingside = to.col() > from.col();
                        int rookCol = isKingside ? 7 : 0;
                        Position rookPos = new Position(from.row(), rookCol);
                        Piece rook = board.getPiece(rookPos);
                        
                        // Check if the rook exists and hasn't moved
                        if (rook != null && rook.getType() == PieceType.ROOK && 
                            rook.getColor() == color && !rook.hasMoved()) {
                            
                            // Check if the path between king and rook is clear
                            int step = isKingside ? 1 : -1;
                            for (int c = from.col() + step; c != rookCol; c += step) {
                                if (board.getPiece(new Position(from.row(), c)) != null) {
                                    return false;
                                }
                            }
                            
                            // Check if the king is in check or passes through check
                            // This would require a more complex implementation to check for attacks
                            // For simplicity, we'll skip this check for now
                            
                            return true;
                        }
                    }
                    
                    return false;
                }
            };
        };
    }
    
    public Piece getPiece(Position position) {
        if (position.isValid()) {
            return squares[position.row()][position.col()];
        }
        return null;
    }
    
    public void movePiece(Position from, Position to) {
        if (from.isValid() && to.isValid()) {
            Piece piece = squares[from.row()][from.col()];
            
            // Handle castling
            if (piece != null && piece.getType() == PieceType.KING && !piece.hasMoved()) {
                int colDiff = to.col() - from.col();
                if (Math.abs(colDiff) == 2) {
                    // This is a castling move
                    boolean isKingside = colDiff > 0;
                    int rookFromCol = isKingside ? 7 : 0;
                    int rookToCol = isKingside ? to.col() - 1 : to.col() + 1;
                    
                    // Move the rook
                    Position rookFrom = new Position(from.row(), rookFromCol);
                    Position rookTo = new Position(from.row(), rookToCol);
                    Piece rook = squares[rookFrom.row()][rookFrom.col()];
                    squares[rookTo.row()][rookTo.col()] = rook;
                    squares[rookFrom.row()][rookFrom.col()] = null;
                    
                    if (rook != null) {
                        rook.setMoved();
                    }
                }
            }
            
            // Move the piece
            squares[to.row()][to.col()] = piece;
            squares[from.row()][from.col()] = null;
            
            // Mark the piece as moved
            if (piece != null) {
                piece.setMoved();
            }
        }
    }
    
    public boolean isValidMove(Position from, Position to, PieceColor currentTurn) {
        if (!from.isValid() || !to.isValid()) {
            return false;
        }
        
        Piece piece = getPiece(from);
        
        if (piece == null || piece.getColor() != currentTurn) {
            return false;
        }
        
        return piece.isValidMove(this, from, to);
    }
    
    // Check if a square is under attack by any piece of the given color
    public boolean isSquareUnderAttack(Position position, PieceColor attackerColor) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position from = new Position(row, col);
                Piece piece = getPiece(from);
                
                if (piece != null && piece.getColor() == attackerColor) {
                    if (piece.isValidMove(this, from, position)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
