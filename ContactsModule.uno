using Fuse;
using Fuse.Scripting;
using Uno;
using Uno.UX;
using Uno.Compiler.ExportTargetInterop;

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
    extern(ANDROID) bool AddContactJava(string json)
    @{
        try {
            return com.fuse.ContactsModule.addContact(new org.json.JSONObject(json));
        } catch (org.json.JSONException e) {
            android.util.Log.e("ContactsModule", e.toString());
            return false;
        }
    @}
}
