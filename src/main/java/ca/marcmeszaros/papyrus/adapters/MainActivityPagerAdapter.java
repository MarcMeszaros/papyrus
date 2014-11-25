package ca.marcmeszaros.papyrus.adapters;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import ca.marcmeszaros.papyrus.fragments.BookListFragment;
import ca.marcmeszaros.papyrus.fragments.LibraryListFragment;
import ca.marcmeszaros.papyrus.fragments.LoanListFragment;

public class MainActivityPagerAdapter extends FragmentPagerAdapter {

    public static final int POSITION_BOOKS = 0;
    public static final int POSITION_LOANS = 1;
    public static final int POSITION_LIBRARIES = 2;

    public MainActivityPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case POSITION_BOOKS:
                return BookListFragment.getInstance();
            case POSITION_LOANS:
                return LoanListFragment.getInstance();
            case POSITION_LIBRARIES:
                return LibraryListFragment.getInstance();
            default:
                return BookListFragment.getInstance();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case POSITION_BOOKS:
                return "Books";
            case POSITION_LOANS:
                return "Loans";
            case POSITION_LIBRARIES:
                return "Libraries";
            default:
                return "Books";
        }
    }
}
