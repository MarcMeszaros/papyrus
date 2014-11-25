package ca.marcmeszaros.papyrus.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ca.marcmeszaros.papyrus.fragments.BooksListFragment;
import ca.marcmeszaros.papyrus.fragments.LibraryListFragment;
import ca.marcmeszaros.papyrus.fragments.LoansListFragment;

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
                return new BooksListFragment();
            case POSITION_LOANS:
                return new LoansListFragment();
            case POSITION_LIBRARIES:
                return new LibraryListFragment();
            default:
                return new BooksListFragment();
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
