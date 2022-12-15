using Fuse;
using Fuse.Scripting;
using Uno;
using Uno.UX;
using Uno.Compiler.ExportTargetInterop;
using Uno.Threading;

[Require("Source.Include", "ContactsModule/ContactsModule.h")]
extern(IOS) partial class ContactsModule
{
    readonly ObjC.Object self = CreateObjCObject();

    [Foreign(Language.ObjC)]
    static ObjC.Object CreateObjCObject()
    @{
        return [[::ContactsModule alloc] init];
    @}

    [Foreign(Language.ObjC)]
    static bool AddContactObjC(ObjC.Object self, string json)
    @{
        return [(::ContactsModule*)self addContact:json];
    @}
}
