using Fuse;
using Fuse.Scripting;
using Uno;
using Uno.UX;
using Uno.Compiler.ExportTargetInterop;
using Uno.Compiler.ExportTargetInterop.Android;
using Uno.Threading;

extern(ANDROID)
partial class ContactsModule
{
    [Foreign(Language.Java)]
    bool HasPermissionJava()
    @{
        return com.fuse.ContactsModule.hasPermission();
    @}

    [Foreign(Language.Java)]
    void RequestPermissionJava(Promise<bool> promise)
    @{
        com.fuse.ContactsModule.requestPermission(promise);
    @}

    [Foreign(Language.Java)]
    bool AddContactJava(string json)
    @{
        try {
            return com.fuse.ContactsModule.addContact(new org.json.JSONObject(json));
        } catch (org.json.JSONException e) {
            android.util.Log.e("ContactsModule", e.toString());
            return false;
        }
    @}

    [Foreign(Language.Java), ForeignFixedName]
    static void resolveContactsModulePromise(object promise, bool result)
    @{
        @{Resolve(object, bool):Call(promise, result)};
    @}

    [Foreign(Language.Java), ForeignFixedName]
    static void rejectContactsModulePromise(object promise, string reason)
    @{
        @{Reject(object, string):Call(promise, reason)};
    @}

    static void Resolve(object promise, bool result)
    {
        ((Promise<bool>)promise).Resolve(result);
    }

    static void Reject(object promise, string reason)
    {
        ((Promise<bool>)promise).Reject(new Exception(reason));
    }
}
