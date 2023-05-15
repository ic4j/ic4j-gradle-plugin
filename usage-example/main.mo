actor class Hello(init : Text){
    stable var name = init;

    public func greet(value : Text) : async Text {
        name := value;
        return "Hello, " # name # "!";
    };

    public shared query func peek() : async Text {
        return name;
    };    
};