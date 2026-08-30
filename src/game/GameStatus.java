package game;

public enum GameStatus {

    /** Game is ongoing with no immediate threat. */
    ACTIVE,

    /** Current player's king is in check but has at least one legal move. */
    CHECK,

    /** Current player's king is in check and has no legal moves — opponent wins. */
    CHECKMATE,

    /** Current player is not in check but has no legal moves — draw. */
    STALEMATE,

    /** 50 full moves (100 half-moves) with no pawn push and no capture — draw. */
    DRAW_FIFTY_MOVE,

    /** Neither side has enough material to deliver checkmate — draw. */
    DRAW_INSUFFICIENT_MATERIAL
}
