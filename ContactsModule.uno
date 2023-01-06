using Fuse;
using Fuse.Scripting;
using Uno;
using Uno.UX;
using Uno.Threading;

[UXGlobalModule]
partial class ContactsModule : NativeModule
{
    static bool _inited;

    public ContactsModule()
    {
        if (_inited)
            return;

        _inited = true;
        Resource.SetGlobalKey(this, "FuseJS/Contacts");

        AddMember(new NativeFunction("hasPermission", (context, args) => {
            if defined(ANDROID)
                return HasPermissionJava();

            return true;
        }));

        AddMember(new NativePromise<bool, bool>("requestPermission", RequestPermission, null));

        AddMember(new NativeFunction("addContact", (context, args) => {
            if (args.Length > 0)
            {
                var contact = context.Stringify(args[0]);

                if defined(ANDROID)
                    return AddContactJava(contact);
                else if defined(IOS)
                    return AddContactObjC(self, contact);
            }

            return null;
        }));
    }

    Future<bool> RequestPermission(object[] args)
    {
        var p = new Promise<bool>();

        if defined(ANDROID)
            RequestPermissionJava(p);
        else
            p.Resolve(true);

        return p;
    }
}
