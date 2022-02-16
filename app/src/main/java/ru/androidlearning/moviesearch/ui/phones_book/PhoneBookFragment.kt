package ru.androidlearning.moviesearch.ui.phones_book

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.databinding.PhoneBookFragmentBinding

class PhoneBookFragment : Fragment() {
    private var _binding: PhoneBookFragmentBinding? = null
    private val phoneBookFragmentBinding get() = _binding!!
    private val phoneBookRecyclerViewAdapter = PhoneBookRecyclerViewAdapter()
    private val phoneBook: MutableList<PhoneBookEntity> = mutableListOf()
    private val permissionToCallPhoneResult: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            context?.let { context ->
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.phone_access_permission_title))
                    .setMessage(getString(R.string.explanation_of_phone_permission))
                    .setNegativeButton(getString(R.string.close_button_text)) { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        } else {
            callPhone("")
        }
    }

    private val permissionToReadContactsResult: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            getContacts()
        } else {
            context?.let { context ->
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.access_to_contacts_title))
                    .setMessage(getString(R.string.explanation_of_contacts_permission))
                    .setNegativeButton(getString(R.string.close_button_text)) { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }
    }

    private val onPhoneNumberClickListener: PhoneBookRecyclerViewAdapter.OnItemClickListener = object : PhoneBookRecyclerViewAdapter.OnItemClickListener {
        override fun onClick(phoneNumber: String?) {
            context?.let {
                when {
                    ContextCompat.checkSelfPermission(it, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED -> {
                        callPhone(phoneNumber)
                    }
                    shouldShowRequestPermissionRationale(android.Manifest.permission.CALL_PHONE) -> {
                        requestPhoneCallPermissionWithDialog()
                    }
                    else -> {
                        permissionToCallPhoneResult.launch(android.Manifest.permission.CALL_PHONE)
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = PhoneBookFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PhoneBookFragmentBinding.inflate(inflater, container, false)

        return phoneBookFragmentBinding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        phoneBookFragmentBinding.phonesBookRecyclerView.adapter = phoneBookRecyclerViewAdapter
        phoneBookRecyclerViewAdapter.setOnItemClickListener(onPhoneNumberClickListener)
        if (savedInstanceState == null) {
            fillPhoneBook()
        } else {
            phoneBookFragmentBinding.phoneBookFragmentLoadingLayout.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        phoneBookRecyclerViewAdapter.clearOnItemClickListener()
        super.onDestroy()
    }

    private fun fillPhoneBook() {
        context?.let {
            when {
                ContextCompat.checkSelfPermission(it, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED -> {
                    getContacts()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS) -> {
                    requestContactsPermissionWithDialog()
                }
                else -> {
                    permissionToReadContactsResult.launch(android.Manifest.permission.READ_CONTACTS)
                }
            }
        }
    }

    private fun requestContactsPermissionWithDialog() {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.access_to_contacts_title))
                .setMessage(getString(R.string.explanation_of_contacts_permission))
                .setPositiveButton(getString(R.string.grant_access_button_text)) { _, _ ->
                    permissionToReadContactsResult.launch(android.Manifest.permission.READ_CONTACTS)
                }
                .setNegativeButton(getString(R.string.negative_button_text)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private fun requestPhoneCallPermissionWithDialog() {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.phone_access_permission_title))
                .setMessage(getString(R.string.explanation_of_phone_permission))
                .setPositiveButton(getString(R.string.grant_access_button_text)) { _, _ ->
                    permissionToCallPhoneResult.launch(android.Manifest.permission.CALL_PHONE)
                }
                .setNegativeButton(getString(R.string.negative_button_text)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    @SuppressLint("Range")
    private fun getContacts() {
        Thread {
            context?.let { context ->
                val contentResolver: ContentResolver = context.contentResolver
                val contactsCursor: Cursor? = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC"
                )
                contactsCursor?.let {
                    while (contactsCursor.moveToNext()) {
                        val contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Data._ID))
                        val hasPhoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                        if (hasPhoneNumber.toInt() > 0) {
                            val phonesCursor: Cursor? = context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(contactId),
                                null
                            )
                            phonesCursor?.let {
                                while (phonesCursor.moveToNext()) {
                                    val phoneNumber = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                    val contactName = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                                    phoneBook.add(PhoneBookEntity(contactName, phoneNumber))
                                }
                            }
                            phonesCursor?.close()
                        }
                    }
                }
                contactsCursor?.close()
            }
            activity?.runOnUiThread { setData() }
        }.start()
    }

    private fun setData() {
        phoneBookFragmentBinding.phoneBookFragmentLoadingLayout.visibility = View.GONE
        phoneBookRecyclerViewAdapter.setData(phoneBook)
    }

    private fun callPhone(phoneNumber: String?) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
    }
}
