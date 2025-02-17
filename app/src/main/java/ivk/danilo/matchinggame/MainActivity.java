package ivk.danilo.matchinggame;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private final int[] cardResources = new int[]{R.drawable.centurion_helmet, R.drawable.closed_barbute, R.drawable.dwarf_helmet, R.drawable.elf_helmet, R.drawable.turban, R.drawable.viking_helmet, R.drawable.war_bonnet, R.drawable.warlord_helmet};
    private final int cardBackgroundResource = R.drawable.card_background;
    private final Handler handler = new Handler();
    private final Map<Integer, Integer> pairPenalties = new HashMap<>();
    private int[][] cards;
    private ImageView[][] cardImageViews;
    private boolean[][] cardFlipped;
    private boolean[][] cardMatched;
    private int flippedCount = 0;
    private int firstFlippedRow = -1;
    private int firstFlippedCol = -1;
    private boolean isProcessing = false;
    private int score = 0;
    private TextView scoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(this.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        this.scoreTextView = this.findViewById(R.id.score);
        this.scoreTextView.setText("0");
        this.initializeCardImageViews();
        this.cardFlipped = new boolean[4][4];
        this.cardMatched = new boolean[4][4];
        this.shuffleCards();
        this.assignCardClickListeners();
    }

    @Contract(pure = true)
    private void initializeCardImageViews() {
        this.cardImageViews = new ImageView[][]{new ImageView[]{this.findViewById(R.id.card_00), this.findViewById(R.id.card_01), this.findViewById(R.id.card_02), this.findViewById(R.id.card_03)}, new ImageView[]{this.findViewById(R.id.card_10), this.findViewById(R.id.card_11), this.findViewById(R.id.card_12), this.findViewById(R.id.card_13)}, new ImageView[]{this.findViewById(R.id.card_20), this.findViewById(R.id.card_21), this.findViewById(R.id.card_22), this.findViewById(R.id.card_23)}, new ImageView[]{this.findViewById(R.id.card_30), this.findViewById(R.id.card_31), this.findViewById(R.id.card_32), this.findViewById(R.id.card_33)}};
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.cardImageViews[i][j].setImageResource(this.cardBackgroundResource);
            }
        }
    }

    @Contract(pure = true)
    private void shuffleCards() {
        int[] deck = new int[16];
        for (int i = 0; i < 8; i++) {
            deck[2 * i] = this.cardResources[i];
            deck[2 * i + 1] = this.cardResources[i];
        }
        Random random = new Random();
        for (int i = deck.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = deck[i];
            deck[i] = deck[j];
            deck[j] = temp;
        }
        this.cards = new int[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(deck, i * 4, this.cards[i], 0, 4);
        }
    }

    private void assignCardClickListeners() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                final int row = i;
                final int col = j;
                this.cardImageViews[i][j].setOnClickListener(v -> this.onCardClicked(row, col));
            }
        }
    }

    @Contract(mutates = "this")
    private void onCardClicked(int row, int col) {
        if (this.isProcessing || this.cardMatched[row][col] || this.cardFlipped[row][col]) {
            return;
        }
        this.cardImageViews[row][col].setImageResource(this.cards[row][col]);
        this.cardFlipped[row][col] = true;
        this.flippedCount++;
        if (this.flippedCount == 1) {
            this.firstFlippedRow = row;
            this.firstFlippedCol = col;
        } else if (this.flippedCount == 2) {
            this.isProcessing = true;
            if (this.cards[row][col] == this.cards[this.firstFlippedRow][this.firstFlippedCol]) {
                this.cardMatched[row][col] = true;
                this.cardMatched[this.firstFlippedRow][this.firstFlippedCol] = true;

                int matchedResourceId = this.cards[row][col];
                Integer penaltyForMatch = this.pairPenalties.getOrDefault(matchedResourceId, 0);
                assert penaltyForMatch != null;

                int pointsForMatch = Math.max(5 - (penaltyForMatch / 2), 1);
                this.score += pointsForMatch;
                this.scoreTextView.setText(String.valueOf(this.score));
                this.pairPenalties.remove(matchedResourceId);
                this.resetFlippedCount();
                this.isProcessing = false;
                this.checkGameCompletion();
            } else {
                int currentCardResource = this.cards[row][col];
                int firstCardResource = this.cards[this.firstFlippedRow][this.firstFlippedCol];

                Integer currentCardPenalty = this.pairPenalties.getOrDefault(currentCardResource, 0);
                assert currentCardPenalty != null;
                this.pairPenalties.put(currentCardResource, currentCardPenalty + 1);

                Integer firstCardPenalty = this.pairPenalties.getOrDefault(firstCardResource, 0);
                assert firstCardPenalty != null;
                this.pairPenalties.put(firstCardResource, firstCardPenalty + 1);

                this.handler.postDelayed(() -> {
                    this.cardImageViews[row][col].setImageResource(this.cardBackgroundResource);
                    this.cardFlipped[row][col] = false;
                    this.cardImageViews[this.firstFlippedRow][this.firstFlippedCol].setImageResource(this.cardBackgroundResource);
                    this.cardFlipped[this.firstFlippedRow][this.firstFlippedCol] = false;
                    this.resetFlippedCount();
                    this.isProcessing = false;
                }, 1000);
            }
        }
    }

    @Contract(mutates = "this")
    private void resetFlippedCount() {
        this.flippedCount = 0;
        this.firstFlippedRow = -1;
        this.firstFlippedCol = -1;
    }

    @Contract(mutates = "this")
    private void checkGameCompletion() {
        boolean complete = true;

        for (int i = 0; i < 4 && complete; i++) {
            for (int j = 0; j < 4; j++) {
                if (!this.cardMatched[i][j]) {
                    complete = false;
                    break;
                }
            }
        }

        if (complete) {
            String gameOver = getString(R.string.game_over);
            Toast.makeText(MainActivity.this, gameOver + this.score, Toast.LENGTH_LONG).show();
            this.handler.postDelayed(this::resetGame, 2000);
        }
    }

    @Contract(mutates = "this")
    private void resetGame() {
        this.score = 0;
        this.pairPenalties.clear();
        this.scoreTextView.setText(String.valueOf(this.score));
        this.resetFlippedCount();
        this.isProcessing = false;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.cardFlipped[i][j] = false;
                this.cardMatched[i][j] = false;
                this.cardImageViews[i][j].setImageResource(this.cardBackgroundResource);
            }
        }
        this.shuffleCards();
    }
}
