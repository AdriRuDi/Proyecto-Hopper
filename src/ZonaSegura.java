public class ZonaSegura extends Zona
{
    public ZonaSegura(String nombre)
    {
        super(nombre);
    }

    @Override
    public void entrarDemogorgon(Demogorgon d) {
        throw new UnsupportedOperationException("No pueden entrar demogorgons en zona segura");
    }
    @Override
    public void salirDemogorgon(Demogorgon d) {
        throw new UnsupportedOperationException("No hay demogorgons en zona segura");
    }
}
