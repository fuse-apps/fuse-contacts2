using Fuse;
using Fuse.Scripting;
using Uno;
using Uno.UX;
using Uno.Compiler.ExportTargetInterop;
using Uno.Compiler.ExportTargetInterop.Android;
using Uno.Threading;

[UXGlobalModule]
class ContactsModule : NativeModule
{
    static bool _inited;
    Function _stringify;

    public ContactsModule()
    {
        if (_inited)
            return;

        _inited = true;
        Resource.SetGlobalKey(this, "FuseJS/Contacts");

        AddMember(new NativeFunction("hasPermission", (context, args) => {
            if defined(ANDROID)
                return HasPermissionJava();

            return false;
        }));

        AddMember(new NativePromise<bool, bool>("requestPermission", RequestPermission, null));

        AddMember(new NativeFunction("addContact", (context, args) => {
            if (args.Length > 0)
            {
                var contact = Stringify(context, args[0]);

                if defined(ANDROID)
                    return AddContactJava(contact);
            }

            return null;
        }));
    }

    string Stringify(Context context, object obj)
    {
        if (_stringify == null)
            _stringify = (Function)context.Evaluate("(Context)", "JSON.stringify");

        var json = _stringify.Call(context, obj);
        return json.ToString();
    }

    [Foreign(Language.Java)]
    extern(ANDROID) bool HasPermissionJava()
    @{
        return com.fuse.ContactsModule.hasPermission();
    @}

    Future<bool> RequestPermission(object[] args)
    {
        var p = new Promise<bool>();

        if defined(ANDROID)
            RequestPermissionJava(p);

        return p;
    }

    [Foreign(Language.Java)]
    extern(ANDROID) void RequestPermissionJava(Promise<bool> promise)
    @{
        com.fuse.ContactsModule.requestPermission(promise);
    @}

    [Foreign(Language.Java)]
    extern(ANDROID) bool AddContactJava(string json)
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
