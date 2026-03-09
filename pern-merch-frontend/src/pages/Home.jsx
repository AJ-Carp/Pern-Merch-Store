import { useEffect, useState } from 'react';
import { getProducts } from '../api/api';
import ProductCard from '../components/ProductCard';
import { Link } from 'react-router-dom';

export default function Home() {
  const [featured, setFeatured] = useState([]);

  useEffect(() => {
    getProducts().then(products => setFeatured(products.slice(0, 4))).catch(() => {});
  }, []);

  return (
    <div>
      {/* Hero */}
      <section className="hero">
        <video className="hero-video" autoPlay loop muted playsInline>
          <source src="/hero.mp4" type="video/mp4" />
        </video>
        <div className="hero-overlay" />
        <div className="hero-content">
          <h1>PERN</h1>
          <p className="hero-subtitle">Official Merch Store</p>
          <Link to="/products" className="btn btn-primary btn-lg">Shop Now</Link>
        </div>
      </section>

      {/* Featured */}
      <section className="section">
        <h2 className="section-title">Featured Merch</h2>
        <div className="product-grid">
          {featured.map(p => <ProductCard key={p.id} product={p} />)}
        </div>
        <div style={{ textAlign: 'center', marginTop: '2rem' }}>
          <Link to="/products" className="btn btn-outline">View All Products</Link>
        </div>
      </section>
    </div>
  );
}
