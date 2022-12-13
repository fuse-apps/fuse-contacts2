package com.fuse;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.uno.UnoObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactsModule {

    static final int WRITE_CONTACTS_REQUEST_CODE = 131525;

    static UnoObject _permissionPromise;

    public static boolean hasPermission() {
        Context context = Activity.getRootActivity().getApplicationContext();
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(UnoObject promise) {
        if (hasPermission()) {
            com.foreign.ExternedBlockHost.resolveContactsModulePromise(promise, true);
            return;
        }

        if (_permissionPromise != null) {
            com.foreign.ExternedBlockHost.rejectContactsModulePromise(_permissionPromise, "Aborted");
            _permissionPromise = null;
        }

        _permissionPromise = promise;
        ActivityCompat.requestPermissions(Activity.getRootActivity(), new String[] { Manifest.permission.WRITE_CONTACTS }, WRITE_CONTACTS_REQUEST_CODE);
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == WRITE_CONTACTS_REQUEST_CODE) {
            boolean result = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (_permissionPromise != null) {
                com.foreign.ExternedBlockHost.resolveContactsModulePromise(_permissionPromise, result);
                _permissionPromise = null;
            }
        }
    }

    public static boolean addContact(JSONObject contact) throws JSONException {
        String givenName = contact.has("givenName") ? contact.getString("givenName") : null;
        String middleName = contact.has("middleName") ? contact.getString("middleName") : null;
        String familyName = contact.has("familyName") ? contact.getString("familyName") : null;
        String prefix = contact.has("prefix") ? contact.getString("prefix") : null;
        String suffix = contact.has("suffix") ? contact.getString("suffix") : null;
        String company = contact.has("company") ? contact.getString("company") : null;
        String jobTitle = contact.has("jobTitle") ? contact.getString("jobTitle") : null;
        String department = contact.has("department") ? contact.getString("department") : null;
        String note = contact.has("note") ? contact.getString("note") : null;

        JSONArray phoneNumbers = contact.has("phoneNumbers") ? contact.getJSONArray("phoneNumbers") : null;
        int numOfPhones = 0;
        String[] phones = null;
        Integer[] phonesTypes = null;
        String[] phonesLabels = null;
        if (phoneNumbers != null) {
            numOfPhones = phoneNumbers.length();
            phones = new String[numOfPhones];
            phonesTypes = new Integer[numOfPhones];
            phonesLabels = new String[numOfPhones];
            for (int i = 0; i < numOfPhones; i++) {
                phones[i] = phoneNumbers.getJSONObject(i).getString("number");
                String label = phoneNumbers.getJSONObject(i).getString("label");
                phonesTypes[i] = mapStringToPhoneType(label);
                phonesLabels[i] = label;
            }
        }

        JSONArray urlAddresses = contact.has("urlAddresses") ? contact.getJSONArray("urlAddresses") : null;
        int numOfUrls = 0;
        String[] urls = null;
        if (urlAddresses != null) {
            numOfUrls = urlAddresses.length();
            urls = new String[numOfUrls];
            for (int i = 0; i < numOfUrls; i++) {
                urls[i] = urlAddresses.getJSONObject(i).getString("url");
            }
        }

        JSONArray emailAddresses = contact.has("emailAddresses") ? contact.getJSONArray("emailAddresses") : null;
        int numOfEmails = 0;
        String[] emails = null;
        Integer[] emailsTypes = null;
        String[] emailsLabels = null;
        if (emailAddresses != null) {
            numOfEmails = emailAddresses.length();
            emails = new String[numOfEmails];
            emailsTypes = new Integer[numOfEmails];
            emailsLabels = new String[numOfEmails];
            for (int i = 0; i < numOfEmails; i++) {
                emails[i] = emailAddresses.getJSONObject(i).getString("email");
                String label = emailAddresses.getJSONObject(i).getString("label");
                emailsTypes[i] = mapStringToEmailType(label);
                emailsLabels[i] = label;
            }
        }

        JSONArray imAddresses = contact.has("imAddresses") ? contact.getJSONArray("imAddresses") : null;
        int numOfIMAddresses = 0;
        String[] imAccounts = null;
        String[] imProtocols = null;
        if (imAddresses != null) {
            numOfIMAddresses = imAddresses.length();
            imAccounts = new String[numOfIMAddresses];
            imProtocols = new String[numOfIMAddresses];
            for (int i = 0; i < numOfIMAddresses; i++) {
                imAccounts[i] = imAddresses.getJSONObject(i).getString("username");
                imProtocols[i] = imAddresses.getJSONObject(i).getString("service");
            }
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder op = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.GIVEN_NAME, givenName)
                .withValue(StructuredName.MIDDLE_NAME, middleName)
                .withValue(StructuredName.FAMILY_NAME, familyName)
                .withValue(StructuredName.PREFIX, prefix)
                .withValue(StructuredName.SUFFIX, suffix);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, note);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                .withValue(Organization.COMPANY, company)
                .withValue(Organization.TITLE, jobTitle)
                .withValue(Organization.DEPARTMENT, department);
        ops.add(op.build());

        op.withYieldAllowed(true);

        for (int i = 0; i < numOfPhones; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Phone.NUMBER, phones[i])
                    .withValue(CommonDataKinds.Phone.TYPE, phonesTypes[i])
                    .withValue(CommonDataKinds.Phone.LABEL, phonesLabels[i]);
            ops.add(op.build());
        }

        for (int i = 0; i < numOfUrls; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Website.URL, urls[i]);
            ops.add(op.build());
        }

        for (int i = 0; i < numOfEmails; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Email.ADDRESS, emails[i])
                    .withValue(CommonDataKinds.Email.TYPE, emailsTypes[i])
                    .withValue(CommonDataKinds.Email.LABEL, emailsLabels[i]);
            ops.add(op.build());
        }

        JSONArray postalAddresses = contact.has("postalAddresses") ? contact.getJSONArray("postalAddresses") : null;
        if (postalAddresses != null) {
            for (int i = 0; i < postalAddresses.length(); i++) {
                JSONObject address = postalAddresses.getJSONObject(i);

                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.StructuredPostal.TYPE, mapStringToPostalAddressType(address.getString("label")))
                        .withValue(CommonDataKinds.StructuredPostal.LABEL, address.getString("label"))
                        .withValue(CommonDataKinds.StructuredPostal.STREET, address.getString("street"))
                        .withValue(CommonDataKinds.StructuredPostal.CITY, address.getString("city"))
                        .withValue(CommonDataKinds.StructuredPostal.REGION, address.getString("state"))
                        .withValue(CommonDataKinds.StructuredPostal.POSTCODE, address.getString("postCode"))
                        .withValue(CommonDataKinds.StructuredPostal.COUNTRY, address.getString("country"));

                ops.add(op.build());
            }
        }

        for (int i = 0; i < numOfIMAddresses; i++)
        {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Im.DATA, imAccounts[i])
                    .withValue(CommonDataKinds.Im.TYPE, CommonDataKinds.Im.TYPE_HOME)
                    .withValue(CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.PROTOCOL_CUSTOM)
                    .withValue(CommonDataKinds.Im.CUSTOM_PROTOCOL, imProtocols[i]);
            ops.add(op.build());
        }

        Context ctx = com.fuse.Activity.getRootActivity().getApplicationContext();
        try {
            ContentResolver cr = ctx.getContentResolver();
            ContentProviderResult[] result = cr.applyBatch(ContactsContract.AUTHORITY, ops);

            if (result != null && result.length > 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e("ContactsModule", e.toString());
        }

        return false;
     }

    private static int mapStringToPhoneType(String label) {
        int phoneType;
        switch (label) {
            case "home":
                phoneType = CommonDataKinds.Phone.TYPE_HOME;
                break;
            case "work":
                phoneType = CommonDataKinds.Phone.TYPE_WORK;
                break;
            case "mobile":
                phoneType = CommonDataKinds.Phone.TYPE_MOBILE;
                break;
            case "main":
                phoneType = CommonDataKinds.Phone.TYPE_MAIN;
                break;
            case "work fax":
                phoneType = CommonDataKinds.Phone.TYPE_FAX_WORK;
                break;
            case "home fax":
                phoneType = CommonDataKinds.Phone.TYPE_FAX_HOME;
                break;
            case "pager":
                phoneType = CommonDataKinds.Phone.TYPE_PAGER;
                break;
            case "work_pager":
                phoneType = CommonDataKinds.Phone.TYPE_WORK_PAGER;
                break;
            case "work_mobile":
                phoneType = CommonDataKinds.Phone.TYPE_WORK_MOBILE;
                break;
            case "other":
                phoneType = CommonDataKinds.Phone.TYPE_OTHER;
                break;
            case "cell":
                phoneType = CommonDataKinds.Phone.TYPE_MOBILE;
                break;
            default:
                phoneType = CommonDataKinds.Phone.TYPE_CUSTOM;
                break;
        }
        return phoneType;
    }

    private static int mapStringToEmailType(String label) {
        int emailType;
        switch (label) {
            case "home":
                emailType = CommonDataKinds.Email.TYPE_HOME;
                break;
            case "work":
                emailType = CommonDataKinds.Email.TYPE_WORK;
                break;
            case "mobile":
                emailType = CommonDataKinds.Email.TYPE_MOBILE;
                break;
            case "other":
                emailType = CommonDataKinds.Email.TYPE_OTHER;
                break;
            case "personal":
                emailType = CommonDataKinds.Email.TYPE_HOME;
                break;
            default:
                emailType = CommonDataKinds.Email.TYPE_CUSTOM;
                break;
        }
        return emailType;
    }

    private static int mapStringToPostalAddressType(String label) {
        int postalAddressType;
        switch (label) {
            case "home":
                postalAddressType = CommonDataKinds.StructuredPostal.TYPE_HOME;
                break;
            case "work":
                postalAddressType = CommonDataKinds.StructuredPostal.TYPE_WORK;
                break;
            default:
                postalAddressType = CommonDataKinds.StructuredPostal.TYPE_CUSTOM;
                break;
        }
        return postalAddressType;
    }
}
