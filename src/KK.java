public sealed class KK permits F, I, J{

}

final class I extends KK{

}

final class J extends KK{

}

non-sealed class F extends KK{

}

class G extends F{

}