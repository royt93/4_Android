package protect.card_locker;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import protect.card_locker.databinding.CardShortcutConfigureActivityBinding;
import protect.card_locker.preferences.Settings;

/**
 * The configuration screen for creating a shortcut.
 */
public class CardShortcutConfigure extends CatimaAppCompatActivity implements LoyaltyCardCursorAdapter.CardAdapterListener {
    private CardShortcutConfigureActivityBinding binding;
    static final String TAG = "Catima";
    private SQLiteDatabase mDatabase;
    private LoyaltyCardCursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = CardShortcutConfigureActivityBinding.inflate(getLayoutInflater());
        mDatabase = new DBHelper(this).getReadableDatabase();

        // Set the result to CANCELED.  This will cause nothing to happen if the
        // aback button is pressed.
        setResult(RESULT_CANCELED);

        setContentView(binding.getRoot());
        Utils.applyWindowInsets(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(R.string.shortcutSelectCard);
        setSupportActionBar(toolbar);

        // If there are no cards, bail
        int cardCount = DBHelper.getLoyaltyCardCount(mDatabase);
        if (cardCount == 0) {
            Toast.makeText(this, R.string.noCardsMessage, Toast.LENGTH_LONG).show();
            finish();
        }

        Cursor cardCursor = DBHelper.getLoyaltyCardCursor(mDatabase, DBHelper.LoyaltyCardArchiveFilter.All);
        mAdapter = new LoyaltyCardCursorAdapter(this, cardCursor, this, null);
        binding.list.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        var layoutManager = (GridLayoutManager) binding.list.getLayoutManager();
        if (layoutManager != null) {
            var settings = new Settings(this);
            layoutManager.setSpanCount(settings.getPreferredColumnCount());
        }
    }

    private void onClickAction(int position) {
        Cursor selected = DBHelper.getLoyaltyCardCursor(mDatabase, DBHelper.LoyaltyCardArchiveFilter.All);
        selected.moveToPosition(position);
        LoyaltyCard loyaltyCard = LoyaltyCard.fromCursor(CardShortcutConfigure.this, selected);

        Log.d(TAG, "Creating shortcut for card " + loyaltyCard.store + "," + loyaltyCard.id);

        ShortcutInfoCompat shortcut = ShortcutHelper.createShortcutBuilder(CardShortcutConfigure.this, loyaltyCard).build();

        setResult(RESULT_OK, ShortcutManagerCompat.createShortcutResultIntent(CardShortcutConfigure.this, shortcut));

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu inputMenu) {
        getMenuInflater().inflate(R.menu.card_details_menu, inputMenu);

        return super.onCreateOptionsMenu(inputMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem inputItem) {
        int id = inputItem.getItemId();

        if (id == R.id.action_display_options) {
            mAdapter.showDisplayOptionsDialog();
            invalidateOptionsMenu();

            return true;
        }

        return super.onOptionsItemSelected(inputItem);
    }

    @Override
    public void onRowClicked(int inputPosition) {
        onClickAction(inputPosition);
    }

    @Override
    public void onRowLongClicked(int inputPosition) {
        // do nothing
    }
}
