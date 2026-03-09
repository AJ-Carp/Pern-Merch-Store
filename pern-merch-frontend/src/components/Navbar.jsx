import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, logoutUser } = useAuth();
  const { cartCount } = useCart();

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-brand">PERN</Link>
        <div className="navbar-links">
          <Link to="/products">Shop</Link>
          {user ? (
            <>
              <Link to="/cart" className="cart-link">
                Cart {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
              </Link>
              <Link to="/orders">Orders</Link>
              {user.role === 'ADMIN' && <Link to="/admin">Admin</Link>}
              <span className="navbar-user">Hi, {user.username}</span>
              <button className="btn btn-sm btn-outline" onClick={logoutUser}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login">Sign In</Link>
              <Link to="/register" className="btn btn-sm btn-primary">Sign Up</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
