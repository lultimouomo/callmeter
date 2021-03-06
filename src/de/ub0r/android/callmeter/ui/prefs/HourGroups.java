/*
 * Copyright (C) 2009-2010 Felix Bechstein, The Android Open Source Project
 * 
 * This file is part of Call Meter 3G.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.callmeter.ui.prefs;

import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import de.ub0r.android.callmeter.R;
import de.ub0r.android.callmeter.data.DataProvider;
import de.ub0r.android.lib.Utils;

/**
 * {@link ListActivity} for setting plans.
 * 
 * @author flx
 */
public class HourGroups extends ListActivity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {
	/**
	 * Adapter binding plans to View.
	 * 
	 * @author flx
	 */
	private class HourGroupAdapter extends ResourceCursorAdapter {
		/**
		 * Default Constructor.
		 * 
		 * @param context
		 *            {@link Context}
		 */
		public HourGroupAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_1, context
					.getContentResolver().query(
							DataProvider.HoursGroup.CONTENT_URI,
							DataProvider.HoursGroup.PROJECTION, null, null,
							null), true);
			this.registerDataSetObserver(new DataSetObserver() {
				@Override
				public void onChanged() {
					super.onChanged();
					HourGroups.this.showEmptyHint();
				}
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void bindView(final View view, final Context ctxt,
				final Cursor cursor) {
			final TextView twTitle = ((TextView) view
					.findViewById(android.R.id.text1));
			twTitle.setText(cursor
					.getString(DataProvider.HoursGroup.INDEX_NAME));
		}

	}

	/** Item menu: edit. */
	private static final int WHICH_EDIT = 0;
	/** Item menu: delete. */
	private static final int WHICH_DELETE = 1;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.setLocale(this);
		this.setTitle(this.getString(R.string.settings) + " > "
				+ this.getString(R.string.rules) + " > "
				+ this.getString(R.string.hours));
		this.setContentView(R.layout.list_ok_add);
		this.setListAdapter(new HourGroupAdapter(this));
		this.getListView().setOnItemClickListener(this);
		this.getListView().setOnItemLongClickListener(this);
		this.findViewById(R.id.ok).setOnClickListener(this);
		this.findViewById(R.id.add).setOnClickListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onResume() {
		super.onResume();
		Utils.setLocale(this);
		this.showEmptyHint();
	}

	/** Set the visibility for the empty groups hint. */
	private void showEmptyHint() {
		int v = View.GONE;
		if (this.getListAdapter().getCount() == 0) {
			v = View.VISIBLE;
		}
		TextView tv = (TextView) this.findViewById(R.id.import_default);
		tv.setText(R.string.empty_groups_);
		tv.setVisibility(v);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onClick(final View v) {
		switch (v.getId()) {
		case R.id.add:
			final ContentValues cv = new ContentValues();
			cv.put(DataProvider.HoursGroup.NAME, this
					.getString(R.string.new_hourgroup));
			final Uri uri = this.getContentResolver().insert(
					DataProvider.HoursGroup.CONTENT_URI, cv);
			final Intent intent = new Intent(this, HourGroupEdit.class);
			intent.setData(uri);
			this.startActivity(intent);
			break;
		case R.id.ok:
			this.finish();
			break;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		final Intent intent = new Intent(// .
				HourGroups.this, HourGroupEdit.class);
		intent.setData(ContentUris.withAppendedId(
				DataProvider.HoursGroup.CONTENT_URI, id));
		HourGroups.this.startActivity(intent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onItemLongClick(final AdapterView<?> parent,
			final View view, final int position, final long id) {
		final Builder builder = new Builder(this);
		builder.setItems(R.array.dialog_edit_delete,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						switch (which) {
						case WHICH_EDIT:
							final Intent intent = new Intent(// .
									HourGroups.this, HourGroupEdit.class);
							intent.setData(ContentUris.withAppendedId(
									DataProvider.HoursGroup.CONTENT_URI, id));
							HourGroups.this.startActivity(intent);
							break;
						case WHICH_DELETE:
							HourGroups.this.getContentResolver().delete(
									ContentUris.withAppendedId(
											DataProvider.HoursGroup.// .
											CONTENT_URI, id), null, null);
							HourGroups.this.showEmptyHint();
							break;
						default:
							break;
						}
					}
				});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.show();
		return true;
	}
}
