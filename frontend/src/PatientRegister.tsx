import React, { useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  MenuItem,
  Grid,
  InputAdornment,
  IconButton,
  Avatar
} from '@mui/material';
import { Link } from 'react-router-dom';
import { Visibility, VisibilityOff } from '@mui/icons-material';

const genders = [
  'Male',
  'Female',
  'Other',
  'Prefer not to say'
];

const PatientRegister: React.FC = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [profilePhoto, setProfilePhoto] = useState<File | null>(null);
  const [profilePhotoUrl, setProfilePhotoUrl] = useState<string | null>(null);

  // Form state
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    dateOfBirth: '',
    gender: '',
    address: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
    password: '',
    confirmPassword: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handlePhotoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setProfilePhoto(e.target.files[0]);
      setProfilePhotoUrl(URL.createObjectURL(e.target.files[0]));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Add validation and submit logic here
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#F5F6F8' }}>
      <Paper sx={{ p: 4, maxWidth: 600, width: '100%' }}>
        <Typography variant="h5" align="center" fontWeight={700} color="#0E151D" gutterBottom>
          Patient Registration
        </Typography>
        <form onSubmit={handleSubmit}>
          {/* Personal Information */}
          <Typography variant="subtitle1" fontWeight={600} sx={{ mt: 2, mb: 1 }}>
            Personal Information
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField label="First Name" name="firstName" value={form.firstName} onChange={handleChange} fullWidth required />
            </Grid>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField label="Last Name" name="lastName" value={form.lastName} onChange={handleChange} fullWidth required />
            </Grid>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField label="Email Address" name="email" value={form.email} onChange={handleChange} fullWidth required type="email" />
            </Grid>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField label="Phone Number" name="phone" value={form.phone} onChange={handleChange} fullWidth required />
            </Grid>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField
                label="Date of Birth"
                name="dateOfBirth"
                value={form.dateOfBirth}
                onChange={handleChange}
                fullWidth
                required
                type="date"
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField
                select
                label="Gender"
                name="gender"
                value={form.gender}
                onChange={handleChange}
                fullWidth
                required
              >
                {genders.map((gender) => (
                  <MenuItem key={gender} value={gender}>{gender}</MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid size={{ xs:12 }}>
              <TextField label="Address" name="address" value={form.address} onChange={handleChange} fullWidth required multiline rows={2} />
            </Grid>
            <Grid size={{ xs:12 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar src={profilePhotoUrl || undefined} sx={{ width: 56, height: 56 }} />
                <Button variant="outlined" component="label">
                  Upload Profile Photo
                  <input type="file" accept="image/*" hidden onChange={handlePhotoChange} />
                </Button>
              </Box>
            </Grid>
          </Grid>

          {/* Emergency Contact */}
          <Typography variant="subtitle1" fontWeight={600} sx={{ mt: 3, mb: 1 }}>
            Emergency Contact
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField label="Emergency Contact Name" name="emergencyContactName" value={form.emergencyContactName} onChange={handleChange} fullWidth required />
            </Grid>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField label="Emergency Contact Phone" name="emergencyContactPhone" value={form.emergencyContactPhone} onChange={handleChange} fullWidth required />
            </Grid>
          </Grid>

          {/* Account Security */}
          <Typography variant="subtitle1" fontWeight={600} sx={{ mt: 3, mb: 1 }}>
            Account Security
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField
                label="Password"
                name="password"
                value={form.password}
                onChange={handleChange}
                fullWidth
                required
                type={showPassword ? 'text' : 'password'}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton onClick={() => setShowPassword((s) => !s)} edge="end">
                        {showPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid size={{ xs:12, sm:6 }}>
              <TextField
                label="Confirm Password"
                name="confirmPassword"
                value={form.confirmPassword}
                onChange={handleChange}
                fullWidth
                required
                type={showConfirmPassword ? 'text' : 'password'}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton onClick={() => setShowConfirmPassword((s) => !s)} edge="end">
                        {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
          </Grid>

          <Button variant="contained" color="primary" fullWidth sx={{ mt: 3 }} type="submit">
            Register
          </Button>
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            Already have an account? <Link to="/patient-login">Login</Link>
          </Box>
        </form>
      </Paper>
    </Box>
  );
};

export default PatientRegister; 